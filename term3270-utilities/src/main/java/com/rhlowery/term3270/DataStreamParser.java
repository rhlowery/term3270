package com.rhlowery.term3270;

import org.openide.util.lookup.ServiceProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.rhlowery.term3270.ProtocolConstants.*;

/**
 * Primary implementation of the {@link IDataStreamParser}
 * for the 3270 protocol.
 *
 * <p>This parser implements a state machine to handle the
 * 3270 byte-stream protocol, including commands (Write,
 * Erase/Write), orders (SBA, SF, SFE, SA), and Write
 * Structured Fields (WSF) for Query and File Transfer.</p>
 *
 * <p>The parser updates a {@link ScreenBuffer} instance as
 * it processes bytes from the host, translating EBCDIC data
 * and attribute orders into a renderable grid of
 * {@link ScreenCell} objects.</p>
 */
public class DataStreamParser implements IDataStreamParser {

  private static final Logger logger =
      LoggerFactory.getLogger(DataStreamParser.class);

  private final ScreenBuffer buffer;
  private EbcdicConverter converter;
  private ParserState state = ParserState.COMMAND;

  private enum ParserState {
    COMMAND, WCC, DATA,
    SBA_1, SBA_2,
    SF_1,
    RA_1, RA_2, RA_CHAR,
    EUA_1, EUA_2,
    SFE_1, SFE_2, SFE_3,
    SA_1, SA_2,
    WSF_LEN_1, WSF_LEN_2, WSF_ID, WSF_DATA
  }

  // ── WSF state ─────────────────────────────────────────
  private Consumer<byte[]> replyCallback;
  private int wsfLen;
  private int wsfProcessed;
  private byte wsfId;
  private IFileTransferHandler fileTransferHandler;
  private ByteArrayOutputStream wsfBuffer;

  // ── Order state ───────────────────────────────────────
  private byte addr1;
  private int stopAddress;
  private int attrCount;
  private byte currentAttrType;
  private FieldAttribute activeFieldAttr = new FieldAttribute();
  private FieldAttribute currentFieldAttr = new FieldAttribute();
  private FieldAttribute globalAttrSet = new FieldAttribute();

  /**
   * Constructs a parser for the given buffer using the
   * default Cp037 codepage.
   *
   * @param buffer The screen buffer to update.
   */
  public DataStreamParser(ScreenBuffer buffer) {
    this.buffer = buffer;
    this.converter = EbcdicConverter.defaultConverter();
  }

  @Override
  public void setConverter(EbcdicConverter converter) {
    this.converter = converter;
  }

  @Override
  public void reset() {
    state = ParserState.COMMAND;
  }

  @Override
  public void startData() {
    state = ParserState.DATA;
  }

  @Override
  public void setReplyCallback(Consumer<byte[]> callback) {
    this.replyCallback = callback;
  }

  @Override
  public void setFileTransferHandler(IFileTransferHandler handler) {
    this.fileTransferHandler = handler;
  }

  /**
   * Provider implementation for the 3270 data stream parser.
   */
  @ServiceProvider(service = IParserProvider.class)
  public static class Provider implements IParserProvider {
    @Override
    public IDataStreamParser createParser(ScreenBuffer buffer) {
      return new DataStreamParser(buffer);
    }

    @Override
    public boolean supports(String emulationType) {
      return "3270".equalsIgnoreCase(emulationType);
    }
  }

  // ════════════════════════════════════════════════════════
  //  Main state machine
  // ════════════════════════════════════════════════════════

  @Override
  public void processByte(byte b) {
    switch (state) {
      case COMMAND   -> handleCommand(b);
      case WCC       -> handleWcc(b);
      case DATA      -> handleData(b);
      case SBA_1     -> { addr1 = b; state = ParserState.SBA_2; }
      case SBA_2     -> {
        int addr = AddressConverter.toAddress(addr1, b);
        logger.debug("SBA to address: {}", addr);
        buffer.setCba(addr);
        state = ParserState.DATA;
      }
      case SF_1      -> {
        handleStartField(b);
        activeFieldAttr = new FieldAttribute(b);
        state = ParserState.DATA;
      }
      case RA_1      -> { addr1 = b; state = ParserState.RA_2; }
      case RA_2      -> {
        stopAddress = AddressConverter.toAddress(addr1, b);
        state = ParserState.RA_CHAR;
      }
      case RA_CHAR   -> {
        buffer.repeatToAddress(stopAddress, converter.decode(b));
        state = ParserState.DATA;
      }
      case EUA_1     -> { addr1 = b; state = ParserState.EUA_2; }
      case EUA_2     -> {
        stopAddress = AddressConverter.toAddress(addr1, b);
        buffer.eraseUnprotectedToAddress(stopAddress);
        state = ParserState.DATA;
      }
      case SFE_1     -> {
        attrCount = b & 0xFF;
        currentFieldAttr = new FieldAttribute();
        state = (attrCount > 0)
            ? ParserState.SFE_2 : ParserState.DATA;
      }
      case SFE_2     -> {
        currentAttrType = b;
        state = ParserState.SFE_3;
      }
      case SFE_3     -> {
        applyExtendedAttribute(
            currentFieldAttr, currentAttrType, b);
        attrCount--;
        if (attrCount > 0) {
          state = ParserState.SFE_2;
        } else {
          int cba = buffer.getCba();
          buffer.setAttribute(
              cba / buffer.getCols() + 1,
              cba % buffer.getCols() + 1,
              currentFieldAttr);
          activeFieldAttr = currentFieldAttr;
          buffer.setCba(cba + 1);
          state = ParserState.DATA;
        }
      }
      case SA_1      -> {
        currentAttrType = b;
        state = ParserState.SA_2;
      }
      case SA_2      -> {
        applyExtendedAttribute(
            globalAttrSet, currentAttrType, b);
        state = ParserState.DATA;
      }
      case WSF_LEN_1 -> {
        wsfLen = (b & 0xFF) << 8;
        state = ParserState.WSF_LEN_2;
      }
      case WSF_LEN_2 -> {
        wsfLen |= (b & 0xFF);
        wsfProcessed = 2;
        wsfBuffer = new ByteArrayOutputStream();
        state = ParserState.WSF_ID;
      }
      case WSF_ID    -> {
        wsfId = b;
        wsfProcessed++;
        wsfBuffer.write(b & 0xFF);
        logger.debug("WSF SF-type: 0x{}",
            Integer.toHexString(wsfId & 0xFF));
        state = (wsfProcessed < wsfLen)
            ? ParserState.WSF_DATA
            : ParserState.COMMAND;
      }
      case WSF_DATA  -> {
        wsfBuffer.write(b & 0xFF);
        wsfProcessed++;
        if (wsfProcessed >= wsfLen) {
          handleStructuredField(wsfId, wsfBuffer.toByteArray());
          // There may be more SFs in this WSF record;
          // parser.reset() (called on EOR) will clean up.
          state = ParserState.WSF_LEN_1;
        }
      }
    }
  }

  // ════════════════════════════════════════════════════════
  //  Command handling
  // ════════════════════════════════════════════════════════

  private boolean isWrite(int val) {
    return val == (CMD_WRITE & 0xFF)
        || val == (CMD_WRITE_SNA & 0xFF);
  }

  private boolean isEraseWrite(int val) {
    return val == (CMD_ERASE_WRITE & 0xFF)
        || val == (CMD_ERASE_WRITE_SNA & 0xFF)
        || val == (CMD_ERASE_WRITE_ALTERNATE & 0xFF)
        || val == (CMD_ERASE_WRITE_ALTERNATE_SNA & 0xFF);
  }

  private boolean isEAU(int val) {
    return val == (CMD_ERASE_ALL_UNPROTECTED & 0xFF)
        || val == (CMD_ERASE_ALL_UNPROTECTED_SNA & 0xFF);
  }

  private boolean isWSF(int val) {
    return val == (CMD_WSF & 0xFF)
        || val == (CMD_WSF_SNA & 0xFF);
  }

  private boolean isRead(int val) {
    return val == (CMD_READ_BUFFER & 0xFF)
        || val == (CMD_READ_BUFFER_SNA & 0xFF)
        || val == (CMD_READ_MODIFIED & 0xFF)
        || val == (CMD_READ_MODIFIED_SNA & 0xFF)
        || val == (CMD_READ_MODIFIED_ALL & 0xFF)
        || val == (CMD_READ_MODIFIED_ALL_SNA & 0xFF);
  }

  /**
   * Processes an initial 3270 command byte and transitions
   * the state machine.
   *
   * @param b The command byte.
   */
  private void handleCommand(byte b) {
    int val = b & 0xFF;
    if (val == 0x00) {
      return; // Ignore NULL/padding between commands
    }
    logger.debug("CMD: 0x{}", Integer.toHexString(val));

    if (isWrite(val) || isEraseWrite(val)) {
      if (isEraseWrite(val)) {
        buffer.clear();
      }
      state = ParserState.WCC;
    } else if (isEAU(val)) {
      buffer.eraseInput();
      buffer.setKeyboardLocked(false);
      state = ParserState.COMMAND;
    } else if (isRead(val)) {
      handleRead(val);
      state = ParserState.COMMAND;
    } else if (isWSF(val)) {
      state = ParserState.WSF_LEN_1;
    } else {
      logger.warn(
          "Unknown 3270 command: 0x{}",
          Integer.toHexString(val));
    }
  }

  private void handleRead(int cmd) {
    logger.info("Host requested READ: 0x{}", Integer.toHexString(cmd));
    buffer.setKeyboardLocked(false);
    
    if (replyCallback == null) return;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // Default response for Read Modified: 
    // AID (ENTER) + Cursor Address + Modified Data
    try {
      out.write(AIDKey.ENTER.getCode());
      byte[] cursorAddr = AddressConverter.fromAddress(buffer.getCursorAddress());
      out.write(cursorAddr[0]);
      out.write(cursorAddr[1]);
      
      if (cmd == (CMD_READ_BUFFER & 0xFF) || cmd == (CMD_READ_BUFFER_SNA & 0xFF)) {
        // For Read Buffer, we should send everything. For now, we'll send empty data.
      } else {
        out.write(buffer.readModified());
      }
      
      replyCallback.accept(out.toByteArray());
    } catch (IOException e) {
      logger.error("Failed to generate read response", e);
    }
  }

  // ════════════════════════════════════════════════════════
  //  WCC
  // ════════════════════════════════════════════════════════

  private void handleWcc(byte b) {
    int val = b & 0xFF;
    logger.debug("WCC: 0x{}", Integer.toHexString(val));
    if ((val & 0x02) != 0) {
      buffer.setKeyboardLocked(false);
      logger.debug("WCC: keyboard unlocked");
    }
    if ((val & 0x01) != 0) {
      buffer.resetMdt();
    }
    state = ParserState.DATA;
  }

  // ════════════════════════════════════════════════════════
  //  Data / Orders
  // ════════════════════════════════════════════════════════

  private void handleData(byte b) {
    int val = b & 0xFF;
    if (val == 0x11) {        // SBA
      state = ParserState.SBA_1;
    } else if (val == 0x1D) { // SF
      state = ParserState.SF_1;
    } else if (val == 0x05) { // PT
      buffer.setCba(
          buffer.findNextUnprotected(buffer.getCba()));
    } else if (val == 0x13) { // IC
      buffer.setCursorAddress(buffer.getCba());
    } else if (val == 0x3C) { // RA
      state = ParserState.RA_1;
    } else if (val == 0x12) { // EUA
      state = ParserState.EUA_1;
    } else if (val == 0x29) { // SFE
      state = ParserState.SFE_1;
    } else if (val == (ORDER_SA & 0xFF)) {
      state = ParserState.SA_1;
    } else {
      // Character data
      int r = buffer.getCba() / buffer.getCols() + 1;
      int c = buffer.getCba() % buffer.getCols() + 1;
      ScreenCell cell = buffer.getCell(r, c);

      FieldAttribute baseAttr =
          buffer.findFieldAttribute(buffer.getCba());
      if (baseAttr == null) {
        baseAttr = activeFieldAttr;
      }

      FieldAttribute merged = merge(baseAttr, globalAttrSet);
      cell.setFieldAttribute(merged);

      buffer.writeRawAtCba(converter.decode(b));
    }
  }

  // ════════════════════════════════════════════════════════
  //  Structured Field handling (inside WSF)
  // ════════════════════════════════════════════════════════

  /**
   * Dispatches a complete structured field received inside
   * a WSF command.
   *
   * @param sfType The structured field type byte.
   * @param data   The SF data (type byte + payload).
   */
  private void handleStructuredField(byte sfType, byte[] data) {
    int type = sfType & 0xFF;
    logger.debug(
        "Processing SF type 0x{}, {} bytes",
        Integer.toHexString(type), data.length);

    if (type == (SF_READ_PARTITION & 0xFF)) {
      handleReadPartition(data);
    } else if (type == (SF_TYPE_FILE_TRANSFER & 0xFF) && fileTransferHandler != null) {
      fileTransferHandler.startField(sfType);
      for (int i = 1; i < data.length; i++) {
        fileTransferHandler.processFileData(data[i]);
      }
      fileTransferHandler.endField();
    }
  }

  /**
   * Handles a Read Partition structured field.
   *
   * <p>Extracts the partition ID and mode, and responds
   * to Query requests with a Query Reply.</p>
   *
   * @param data The SF data (type + pid + mode).
   */
  private void handleReadPartition(byte[] data) {
    // data[0] = SF type (0x01, already known)
    // data[1] = partition ID (0xFF = all)
    // data[2] = mode (0x02 = Query, 0x03 = Query List)
    if (data.length >= 3) {
      int pid = data[1] & 0xFF;
      int mode = data[2] & 0xFF;
      logger.info(
          "Read Partition: pid=0x{}, mode=0x{}",
          Integer.toHexString(pid),
          Integer.toHexString(mode));

      if (mode == (RP_QUERY & 0xFF)
          || mode == (RP_QUERY_LIST & 0xFF)) {
        sendQueryReply();
      }
    }
  }

  // ════════════════════════════════════════════════════════
  //  Query Reply
  // ════════════════════════════════════════════════════════

  private void sendQueryReply() {
    if (replyCallback == null) {
      return;
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(AID_STRUCTURED_FIELD & 0xFF);

    // Usable Area Query Reply
    byte[] usableArea = {
      0x00, 0x17,                        // Length = 23
      SF_TYPE_QUERY_REPLY,               // 0x81
      (byte) 0x81,                       // QCODE: Usable Area
      0x01,                              // 12/14 bit addr
      0x00, 0x00,                        // flags
      0x00, 0x50,                        // width 80
      0x00, 0x18,                        // height 24
      0x01,                              // units (mm)
      0x00, (byte) 0xF4, 0x00, (byte) 0x90, // Xr, Yr
      0x00, 0x07,                        // AW
      0x00, (byte) 0x0C,                 // AH
      0x00, 0x50,                        // buf width 80
      0x00, 0x18                         // buf height 24
    };

    // Summary Query Reply (required)
    byte[] summary = {
      0x00, 0x08,                        // Length = 8
      SF_TYPE_QUERY_REPLY,               // 0x81
      (byte) 0x80,                       // QCODE: Summary
      (byte) 0x80,                       // Summary
      (byte) 0x81,                       // Usable Area
      (byte) 0x87,                       // Highlight
      SF_QCODE_FILE_TRANSFER             // File Transfer
    };

    // Highlight Query Reply
    byte[] highlight = {
      0x00, 0x0D,                        // Length = 13
      SF_TYPE_QUERY_REPLY,               // 0x81
      (byte) 0x87,                       // QCODE: Highlight
      0x05,                              // 5 pairs
      0x00, (byte) 0xF0,                 // Default
      (byte) 0xF1, (byte) 0xF1,         // Blink
      (byte) 0xF2, (byte) 0xF2,         // Reverse
      (byte) 0xF4, (byte) 0xF4,         // Underscore
      (byte) 0xF8, (byte) 0xF8          // Intensify
    };

    // File Transfer Query Reply (required for IND$FILE)
    byte[] fileTransfer = {
      0x00, 0x05,                        // Length = 5
      SF_TYPE_QUERY_REPLY,               // 0x81
      SF_QCODE_FILE_TRANSFER,            // 0xCA
      0x01                               // Flags (0x01 = supported)
    };

    try {
      out.write(summary);
      out.write(usableArea);
      out.write(highlight);
      out.write(fileTransfer);
      logger.info(
          "Sending Query Reply ({} bytes)",
          out.size());
      replyCallback.accept(out.toByteArray());
    } catch (IOException e) {
      logger.error("Failed to send query reply", e);
    }
  }

  // ════════════════════════════════════════════════════════
  //  Field / Attribute helpers
  // ════════════════════════════════════════════════════════

  private void handleStartField(byte b) {
    FieldAttribute attr = new FieldAttribute();
    int val = b & 0xFF;
    attr.setProtected((val & 0x20) != 0);
    attr.setIntensified((val & 0x08) != 0);
    attr.setHidden((val & 0x0C) == 0x0C);
    attr.setModified((val & 0x01) != 0);

    buffer.setAttribute(
        buffer.getCba() / buffer.getCols() + 1,
        buffer.getCba() % buffer.getCols() + 1,
        attr);
    buffer.setCba(buffer.getCba() + 1);
  }

  private void applyExtendedAttribute(
      FieldAttribute attr, byte type, byte value) {
    int v = value & 0xFF;
    switch (type) {
      case (byte) 0xC0 -> {
        FieldAttribute std = new FieldAttribute(value);
        attr.setProtected(std.isProtected());
        attr.setNumeric(std.isNumeric());
        attr.setIntensified(std.isIntensified());
        attr.setHidden(std.isHidden());
        attr.setModified(std.isModified());
      }
      case (byte) 0x42 ->
          attr.setColor(
              FieldAttribute.ExtendedColor.fromCode(v));
      case (byte) 0x41 ->
          attr.setHighlight(
              FieldAttribute.HighlightType.fromCode(v));
    }
  }

  private FieldAttribute merge(
      FieldAttribute base, FieldAttribute global) {
    FieldAttribute result = new FieldAttribute();

    result.setProtected(base.isProtected());
    result.setIntensified(base.isIntensified());
    result.setModified(base.isModified());
    result.setColor(base.getColor());
    result.setHighlight(base.getHighlight());

    if (global.getColor()
        != FieldAttribute.ExtendedColor.NEUTRAL) {
      result.setColor(global.getColor());
    }
    if (global.getHighlight()
        != FieldAttribute.HighlightType.NORMAL) {
      result.setHighlight(global.getHighlight());
    }
    return result;
  }
}
