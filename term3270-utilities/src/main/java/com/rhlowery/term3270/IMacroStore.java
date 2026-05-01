package com.rhlowery.term3270;

import java.io.File;
import java.util.List;

/**
 * Service Provider Interface (SPI) for pluggable macro storage.
 * 
 * <p>Allows different serialization formats (JSON, XML, etc.) to be used 
 * for persisting terminal macros.</p>
 */
public interface IMacroStore {

  /**
   * Saves a list of macro actions to the specified file.
   *
   * @param actions The actions to save.
   * @param file    The destination file.
   * @throws Exception If saving fails.
   */
  void save(List<MacroAction> actions, File file) throws Exception;

  /**
   * Loads a list of macro actions from the specified file.
   *
   * @param file The source file.
   * @return The loaded actions.
   * @throws Exception If loading fails.
   */
  List<MacroAction> load(File file) throws Exception;

  /**
   * Checks if this store supports the specified format.
   *
   * @param format The format string (e.g., "JSON").
   * @return True if supported.
   */
  boolean supports(String format);
}
