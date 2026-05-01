package com.rhlowery.term3270.ui;

import com.rhlowery.term3270.AIDKey;
import java.awt.event.KeyEvent;

/**
 * Service Provider Interface (SPI) for pluggable keyboard layouts.
 * 
 * <p>Allows different physical keyboard mappings to be implemented as 
 * separate modules and discovered at runtime.</p>
 */
public interface IKeyboardLayout {

  /**
   * Maps a physical keyboard event to a logical 3270 AID key.
   *
   * @param e The key event.
   * @return The corresponding AID key, or null if no mapping exists.
   */
  AIDKey mapKey(KeyEvent e);

  /**
   * Returns the name of this keyboard layout.
   *
   * @return The layout name (e.g., "US-English").
   */
  String getName();
}
