package com.rhlowery.term3270;

import java.io.IOException;

/**
 * Service Provider Interface (SPI) for processing file transfer events 
 * and data chunks.
 * 
 * <p>Implementations of this interface are notified by the {@link IDataStreamParser} 
 * when file transfer structured fields are encountered in the host data stream.</p>
 */
public interface IFileTransferHandler {
  /**
   * Processes a single byte of file data.
   *
   * @param b The data byte.
   */
  void processFileData(byte b);

  /**
   * Called when a file transfer structured field starts.
   *
   * @param id The field ID.
   */
  void startField(byte id);

  /**
   * Called when a file transfer structured field ends.
   */
  void endField();

  /**
   * Prepares the handler for a file download.
   *
   * @param localPath The local filesystem path where the file will be saved.
   * @throws IOException If the file cannot be created or opened.
   */
  void startDownload(String localPath) throws IOException;

  /**
   * Initiates an upload of a local file to the host.
   *
   * @param localPath The local source path.
   * @param session   The active terminal session to use for sending data.
   * @throws IOException If the local file cannot be read.
   */
  void startUpload(String localPath, ITerminalSession session) throws IOException;

  /**
   * Returns whether a file transfer operation is currently active.
   *
   * @return True if active.
   */
  boolean isActive();

  /**
   * Finalizes the current transfer, flushing and closing any open resources.
   *
   * @throws IOException If the resources cannot be closed properly.
   */
  void complete() throws IOException;
}
