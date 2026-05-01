package com.rhlowery.term3270;

import org.openide.util.lookup.ServiceProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Service implementation for managing IND$FILE protocol file transfers.
 * 
 * <p>This service handles both downloading files from the host (by listening for 
 * Data Structured Fields) and uploading files to the host (by segmenting local 
 * files into protocol-compliant data chunks).</p>
 * 
 * <p>The implementation follows the IBM standard for structured field file 
 * transfer (ID 0x47) used by TSO and CMS environments.</p>
 */
public class FileTransferService implements IFileTransferHandler {

  /**
   * The local file being used for the current transfer.
   */
  private File targetFile;

  /**
   * Output stream for writing downloaded file data.
   */
  private FileOutputStream outputStream;

  /**
   * Flag indicating if a file transfer is currently in progress.
   */
  private boolean active = false;

  /**
   * Prepares the service for a file download.
   *
   * @param localPath The local filesystem path where the file will be saved.
   * @throws IOException If the file cannot be created or opened.
   */
  public void startDownload(String localPath) throws IOException {
    this.targetFile = new File(localPath);
    this.outputStream = new FileOutputStream(targetFile);
    this.active = true;
  }

  @Override
  public void processFileData(byte b) {
    if (!active || outputStream == null) return;
    try {
      outputStream.write(b);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void startField(byte id) {
    // 0x47 is often used for Data Chunks in IND$FILE
  }

  @Override
  public void endField() {
    // Check if we should close the file based on the next command or a specific EOF marker
  }

  /**
   * Finalizes the current transfer, flushing and closing any open streams.
   *
   * @throws IOException If the stream cannot be closed properly.
   */
  public void complete() throws IOException {
    if (outputStream != null) {
      outputStream.close();
      outputStream = null;
    }
    active = false;
  }

  /**
   * Returns whether a file transfer operation is currently active.
   *
   * @return True if active.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Initiates an upload of a local file to the host.
   *
   * @param localPath The local source path.
   * @param session   The active terminal session to use for sending data.
   * @throws IOException If the local file cannot be read.
   */
  public void startUpload(String localPath, ITerminalSession session) throws IOException {
    File sourceFile = new File(localPath);
    byte[] data = Files.readAllBytes(sourceFile.toPath());
    
    // Send in chunks of ~2KB
    int offset = 0;
    while (offset < data.length) {
      int chunkSize = Math.min(2048, data.length - offset);
      sendDataChunk(session, data, offset, chunkSize);
      offset += chunkSize;
    }
  }

  /**
   * Sends a single data chunk to the host as a 3270 structured field.
   *
   * @param session The session to send data through.
   * @param data    The full data array.
   * @param offset  The starting offset in the array.
   * @param len     The length of the chunk.
   */
  private void sendDataChunk(ITerminalSession session, byte[] data, int offset, int len) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(0x88); // AID: Structured Field
    
    int wsfLen = len + 3; // Len(2) + ID(1)
    out.write((wsfLen >> 8));
    out.write((wsfLen & 0xFF));
    out.write(0x47); // Data ID
    out.write(data, offset, len);
    
    session.sendData(out.toByteArray());
  }

  /**
   * Provider implementation for IND$FILE transfer handler.
   */
  @ServiceProvider(service = IFileTransferProvider.class)
  public static class Provider implements IFileTransferProvider {
    @Override
    public IFileTransferHandler createHandler() {
      return new FileTransferService();
    }

    @Override
    public boolean supports(String protocol) {
      return "IND$FILE".equalsIgnoreCase(protocol);
    }
  }
}
