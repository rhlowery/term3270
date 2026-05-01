package com.rhlowery.term3270;

/**
 * Service Provider Interface (SPI) for creating file transfer handlers.
 * 
 * <p>This interface allows different file transfer protocols (e.g., IND$FILE, 
 * Kermit) to be plugged into the terminal emulator dynamically.</p>
 */
public interface IFileTransferProvider {

  /**
   * Creates a new instance of the file transfer handler.
   *
   * @return A new handler instance.
   */
  IFileTransferHandler createHandler();

  /**
   * Checks if this provider supports the specified transfer protocol.
   *
   * @param protocol The protocol name (e.g., "IND$FILE").
   * @return True if supported, false otherwise.
   */
  boolean supports(String protocol);
}
