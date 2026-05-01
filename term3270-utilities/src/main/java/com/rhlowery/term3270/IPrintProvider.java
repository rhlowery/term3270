package com.rhlowery.term3270;

import java.io.File;

/**
 * Service Provider Interface (SPI) for pluggable printing engines.
 * 
 * <p>Allows different export formats (PDF, HTML, etc.) to be implemented 
 * as separate modules and discovered at runtime.</p>
 */
public interface IPrintProvider {

  /**
   * Prints the screen buffer to the specified file.
   *
   * @param buffer     The screen buffer to print.
   * @param targetFile The destination file.
   * @throws Exception If printing fails.
   */
  void print(ScreenBuffer buffer, File targetFile) throws Exception;

  /**
   * Checks if this provider supports the specified format.
   *
   * @param format The format string (e.g., "PDF").
   * @return True if supported.
   */
  boolean supports(String format);
}
