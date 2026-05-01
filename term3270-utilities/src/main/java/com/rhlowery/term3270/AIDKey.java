package com.rhlowery.term3270;

/**
 * Defines the 3270 Attention Identifier (AID) keys used for host communication.
 * 
 * <p>AID keys are sent to the host when the user performs an action that 
 * requires host attention, such as pressing Enter, Clear, or a Function key.</p>
 */
public enum AIDKey {
  /** The primary attention key used to submit input to the host. */
  ENTER,
  /** Clears the screen buffer and resets the partition state. */
  CLEAR,
  /** Program Function key 1. */
  PF1, 
  /** Program Function key 2. */
  PF2, 
  /** Program Function key 3. */
  PF3, 
  /** Program Function key 4. */
  PF4, 
  /** Program Function key 5. */
  PF5, 
  /** Program Function key 6. */
  PF6,
  /** Program Function key 7. */
  PF7, 
  /** Program Function key 8. */
  PF8, 
  /** Program Function key 9. */
  PF9, 
  /** Program Function key 10. */
  PF10, 
  /** Program Function key 11. */
  PF11, 
  /** Program Function key 12. */
  PF12,
  /** Program Function key 13. */
  PF13, 
  /** Program Function key 14. */
  PF14, 
  /** Program Function key 15. */
  PF15, 
  /** Program Function key 16. */
  PF16, 
  /** Program Function key 17. */
  PF17, 
  /** Program Function key 18. */
  PF18,
  /** Program Function key 19. */
  PF19, 
  /** Program Function key 20. */
  PF20, 
  /** Program Function key 21. */
  PF21, 
  /** Program Function key 22. */
  PF22, 
  /** Program Function key 23. */
  PF23, 
  /** Program Function key 24. */
  PF24,
  /** Program Access key 1. */
  PA1, 
  /** Program Access key 2. */
  PA2, 
  /** Program Access key 3. */
  PA3;

  /**
   * Returns the protocol-specific byte code for this AID key.
   *
   * @return The EBCDIC-mapped AID code byte.
   */
  public byte getCode() {
    return switch (this) {
      case ENTER -> (byte) 0x7D;
      case PF1 -> (byte) 0xF1;
      case PF2 -> (byte) 0xF2;
      case PF3 -> (byte) 0xF3;
      case PF4 -> (byte) 0xF4;
      case PF5 -> (byte) 0xF5;
      case PF6 -> (byte) 0xF6;
      case PF7 -> (byte) 0xF7;
      case PF8 -> (byte) 0xF8;
      case PF9 -> (byte) 0xF9;
      case PF10 -> (byte) 0x7A;
      case PF11 -> (byte) 0x7B;
      case PF12 -> (byte) 0x7C;
      case PF13 -> (byte) 0xC1;
      case PF14 -> (byte) 0xC2;
      case PF15 -> (byte) 0xC3;
      case PF16 -> (byte) 0xC4;
      case PF17 -> (byte) 0xC5;
      case PF18 -> (byte) 0xC6;
      case PF19 -> (byte) 0xC7;
      case PF20 -> (byte) 0xC8;
      case PF21 -> (byte) 0xC9;
      case PF22 -> (byte) 0x4A;
      case PF23 -> (byte) 0x4B;
      case PF24 -> (byte) 0x4C;
      case PA1 -> (byte) 0x6C;
      case PA2 -> (byte) 0x6E;
      case PA3 -> (byte) 0x6B;
      case CLEAR -> (byte) 0x6D;
    };
  }
}
