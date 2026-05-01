package com.rhlowery.term3270.ui;

import com.rhlowery.term3270.AIDKey;
import org.openide.util.lookup.ServiceProvider;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of a keyboard layout for standard US keyboards.
 */
@ServiceProvider(service = IKeyboardLayout.class)
public class KeyboardMapper implements IKeyboardLayout {

  /**
   * Internal mapping of Swing key codes to 3270 AID keys.
   */
  private static final Map<Integer, AIDKey> AID_MAP = new HashMap<>();

  static {
    AID_MAP.put(KeyEvent.VK_ENTER, AIDKey.ENTER);
    AID_MAP.put(KeyEvent.VK_ESCAPE, AIDKey.CLEAR);
    AID_MAP.put(KeyEvent.VK_F1, AIDKey.PF1);
    AID_MAP.put(KeyEvent.VK_F2, AIDKey.PF2);
    AID_MAP.put(KeyEvent.VK_F3, AIDKey.PF3);
    AID_MAP.put(KeyEvent.VK_F4, AIDKey.PF4);
    AID_MAP.put(KeyEvent.VK_F5, AIDKey.PF5);
    AID_MAP.put(KeyEvent.VK_F6, AIDKey.PF6);
    AID_MAP.put(KeyEvent.VK_F7, AIDKey.PF7);
    AID_MAP.put(KeyEvent.VK_F8, AIDKey.PF8);
    AID_MAP.put(KeyEvent.VK_F9, AIDKey.PF9);
    AID_MAP.put(KeyEvent.VK_F10, AIDKey.PF10);
    AID_MAP.put(KeyEvent.VK_F11, AIDKey.PF11);
    AID_MAP.put(KeyEvent.VK_F12, AIDKey.PF12);
  }

  @Override
  public AIDKey mapKey(KeyEvent e) {
    return AID_MAP.get(e.getKeyCode());
  }

  @Override
  public String getName() {
    return "US-English";
  }

  /**
   * Deprecated: Use Lookup to find the active IKeyboardLayout.
   *
   * @param e The KeyEvent.
   * @return The corresponding AIDKey.
   */
  @Deprecated
  public static AIDKey mapToAID(KeyEvent e) {
    return AID_MAP.get(e.getKeyCode());
  }
}
