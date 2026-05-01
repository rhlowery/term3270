package com.rhlowery.term3270;

/**
 * Utility class for converting between 3270 buffer addresses and protocol bytes.
 * 
 * <p>3270 addresses are typically 12-bit or 14-bit. This utility handles the 
 * standard 12-bit encoding where 6 bits are mapped to specific EBCDIC-friendly 
 * byte values to avoid control character conflicts.</p>
 */
public class AddressConverter {

  /**
   * Table for mapping a 6-bit value (0-63) to its corresponding 3270 
   * address byte.
   */
  private static final byte[] TO_CODE = {
    (byte)0x40, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, 
    (byte)0xC8, (byte)0xC9, (byte)0x4A, (byte)0x4B, (byte)0x4C, (byte)0x4D, (byte)0x4E, (byte)0x4F,
    (byte)0x50, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6, (byte)0xD7, 
    (byte)0xD8, (byte)0xD9, (byte)0x5A, (byte)0x5B, (byte)0x5C, (byte)0x5D, (byte)0x5E, (byte)0x5F,
    (byte)0x60, (byte)0x61, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6, (byte)0xE7, 
    (byte)0xE8, (byte)0xE9, (byte)0x6A, (byte)0x6B, (byte)0x6C, (byte)0x6D, (byte)0x6E, (byte)0x6F,
    (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, 
    (byte)0xF8, (byte)0xF9, (byte)0x7A, (byte)0x7B, (byte)0x7C, (byte)0x7D, (byte)0x7E, (byte)0x7F
  };

  /**
   * Reverse lookup table for mapping 3270 address bytes back to 6-bit values.
   */
  private static final int[] FROM_CODE = new int[256];

  static {
    for (int i = 0; i < 256; i++) FROM_CODE[i] = -1;
    for (int i = 0; i < 64; i++) {
      FROM_CODE[TO_CODE[i] & 0xFF] = i;
    }
  }

  /**
   * Translates two mapped bytes into a 12-bit buffer address.
   *
   * @param b1 First byte.
   * @param b2 Second byte.
   * @return The 12-bit address.
   */
  public static int toAddress(byte b1, byte b2) {
    int v1 = FROM_CODE[b1 & 0xFF];
    int v2 = FROM_CODE[b2 & 0xFF];
    if (v1 == -1 || v2 == -1) return 0; // Fallback or error
    return (v1 << 6) | v2;
  }

  /**
   * Translates a 12-bit buffer address into two mapped bytes.
   *
   * @param addr The 12-bit address.
   * @return The two bytes.
   */
  public static byte[] fromAddress(int addr) {
    byte[] result = new byte[2];
    result[0] = TO_CODE[(addr >> 6) & 0x3F];
    result[1] = TO_CODE[addr & 0x3F];
    return result;
  }
}
