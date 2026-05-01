package com.rhlowery.term3270;

/**
 * Service Provider Interface (SPI) for creating protocol-specific data 
 * stream parsers.
 * 
 * <p>Implementations of this interface should be registered using the 
 * {@code @ServiceProvider} annotation to allow dynamic discovery via 
 * {@code Lookup}.</p>
 */
public interface IParserProvider {

  /**
   * Creates a new instance of the data stream parser.
   *
   * @param buffer The screen buffer to be updated by the parser.
   * @return A new parser instance.
   */
  IDataStreamParser createParser(ScreenBuffer buffer);

  /**
   * Checks if this provider supports the specified emulation type.
   *
   * @param emulationType The emulation type (e.g., "3270", "5250").
   * @return True if supported, false otherwise.
   */
  boolean supports(String emulationType);
}
