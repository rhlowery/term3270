package com.rhlowery.term3270;

import java.util.function.Consumer;

/**
 * Defines the contract for protocol-specific data stream parsers.
 * 
 * <p>Implementations of this interface are responsible for consuming raw byte 
 * streams from the host (e.g., via TN3270 or TN5250) and updating the 
 * {@link ScreenBuffer} accordingly. The parser maintains an internal state 
 * machine to handle multi-byte orders and structured fields.</p>
 */
public interface IDataStreamParser {

  /**
   * Processes a single byte of protocol data.
   *
   * @param b The byte to process.
   */
  void processByte(byte b);

  /**
   * Resets the parser state to expect a new command.
   */
  void reset();

  /**
   * Sets the EBCDIC converter used for character
   * translation.
   *
   * @param converter The converter instance.
   */
  void setConverter(EbcdicConverter converter);

  /**
   * Transitions the parser directly into the DATA
   * processing state, bypassing the initial
   * COMMAND and WCC states. This is intended for
   * test harnesses that need to feed individual
   * 3270 orders without a full Write+WCC preamble.
   */
  void startData();

  /**
   * Sets a callback to be invoked when the parser
   * needs to send data back to the host (e.g., Query Reply).
   *
   * @param callback The callback receiving the byte array.
   */
  void setReplyCallback(Consumer<byte[]> callback);

  /**
   * Sets the handler for file transfer structured fields.
   *
   * @param handler The handler instance.
   */
  void setFileTransferHandler(IFileTransferHandler handler);
}
