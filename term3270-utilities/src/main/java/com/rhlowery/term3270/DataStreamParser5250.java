package com.rhlowery.term3270;

import org.openide.util.lookup.ServiceProvider;
import java.util.function.Consumer;

/**
 * Data stream parser for the IBM 5250 protocol.
 */
public class DataStreamParser5250 implements IDataStreamParser {

  private final ScreenBuffer buffer;
  private EbcdicConverter converter;
  private ParserState state = ParserState.COMMAND;

  private enum ParserState {
    COMMAND, WTD_CC1, WTD_CC2, DATA, SBA_ROW, SBA_COL, SF_ATTR
  }

  /**
   * Constructs a 5250 parser for the given buffer.
   *
   * @param buffer The screen buffer to update.
   */
  public DataStreamParser5250(ScreenBuffer buffer) {
    this.buffer = buffer;
    this.converter = EbcdicConverter.defaultConverter();
  }

  @Override
  public void processByte(byte b) {
    switch (state) {
      case COMMAND -> {
        if (b == 0x04) { // WTD
          state = ParserState.WTD_CC1;
        }
      }
      case WTD_CC1 -> state = ParserState.WTD_CC2;
      case WTD_CC2 -> state = ParserState.DATA;
      case DATA -> handleData(b);
      case SBA_ROW -> {
        int row = b & 0xFF;
        buffer.setCba((row - 1) * buffer.getCols());
        state = ParserState.SBA_COL;
      }
      case SBA_COL -> {
        int col = b & 0xFF;
        buffer.setCba(buffer.getCba() + (col - 1));
        state = ParserState.DATA;
      }
      case SF_ATTR -> {
        // Basic SF implementation
        FieldAttribute attr = new FieldAttribute();
        buffer.setAttribute(buffer.getCba() / buffer.getCols() + 1, 
                            buffer.getCba() % buffer.getCols() + 1, attr);
        buffer.setCba(buffer.getCba() + 1);
        state = ParserState.DATA;
      }
    }
  }

  private void handleData(byte b) {
    if (b == 0x11) { // SBA
      state = ParserState.SBA_ROW;
    } else if (b == 0x1D) { // SF
      state = ParserState.SF_ATTR;
    } else {
      buffer.writeRawAtCba(converter.decode(b));
    }
  }

  @Override
  public void reset() {
    // Initial stub
  }

  @Override
  public void setConverter(EbcdicConverter converter) {
    this.converter = converter;
  }

  @Override
  public void startData() {
    state = ParserState.DATA;
  }

  @Override
  public void setReplyCallback(Consumer<byte[]> callback) {
    // Initial stub
  }

  @Override
  public void setFileTransferHandler(IFileTransferHandler handler) {
    // Initial stub
  }

  /**
   * Provider implementation for the 5250 data stream parser.
   */
  @ServiceProvider(service = IParserProvider.class)
  public static class Provider implements IParserProvider {
    @Override
    public IDataStreamParser createParser(ScreenBuffer buffer) {
      return new DataStreamParser5250(buffer);
    }

    @Override
    public boolean supports(String emulationType) {
      return "5250".equalsIgnoreCase(emulationType);
    }
  }
}
