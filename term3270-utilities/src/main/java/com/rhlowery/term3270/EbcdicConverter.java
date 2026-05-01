package com.rhlowery.term3270;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Utility for bidirectional translation between EBCDIC and ASCII/Unicode characters.
 * 
 * <p>This converter supports various EBCDIC codepages (e.g., Cp037 for US/Canada, 
 * Cp285 for UK, Cp1140 for Euro support) by wrapping Java's native 
 * {@link Charset} functionality. It handles mapping of unmappable characters 
 * and control codes to ensure consistent terminal rendering.</p>
 *
 * <p>Both instance-based methods (supporting specific codepages) and 
 * static convenience methods (using the default Cp037) are provided.</p>
 */
public class EbcdicConverter {

  /** Default codepage (US/Canada EBCDIC). */
  public static final String DEFAULT_CODEPAGE = "Cp037";

  /**
   * The default converter instance using Cp037.
   */
  private static final EbcdicConverter DEFAULT =
      new EbcdicConverter(DEFAULT_CODEPAGE);

  /**
   * The underlying Java charset for this converter.
   */
  private final Charset charset;

  /**
   * Constructs a converter for the specified EBCDIC
   * codepage.
   *
   * @param codepage A Java charset name such as
   *     {@code "Cp037"}, {@code "Cp285"}, or
   *     {@code "Cp1140"}.
   * @throws UnsupportedCharsetException
   *     if the codepage is not supported.
   */
  public EbcdicConverter(String codepage) {
    this.charset = Charset.forName(codepage);
  }

  /**
   * Returns the default converter (Cp037).
   *
   * @return The default converter instance.
   */
  public static EbcdicConverter defaultConverter() {
    return DEFAULT;
  }

  /**
   * Returns the charset name for this converter.
   *
   * @return The charset name.
   */
  public String getCodepage() {
    return charset.name();
  }

  // ----- Instance methods -----

  /**
   * Translates an EBCDIC byte to an ASCII/Unicode
   * character using this converter's codepage.
   *
   * @param b The EBCDIC byte.
   * @return The translated character, or a space if
   *     the byte cannot be decoded.
   */
  public char decode(byte b) {
    CharsetDecoder dec = charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    ByteBuffer in = ByteBuffer.wrap(new byte[]{b});
    CharBuffer out = CharBuffer.allocate(1);
    dec.decode(in, out, true);
    dec.flush(out);
    out.flip();
    if (out.hasRemaining()) {
      char c = out.get();
      // Map control characters / null to space, but preserve actual NULL
      if (c == 0x00) {
        return '\0';
      }
      if (c < 0x20 && c != '\n' && c != '\r') {
        return ' ';
      }
      return c;
    }
    return ' ';
  }

  /**
   * Translates an ASCII/Unicode character to an EBCDIC
   * byte using this converter's codepage.
   *
   * @param c The character to encode.
   * @return The EBCDIC byte, or 0x40 (space) if the
   *     character cannot be encoded.
   */
  public byte encode(char c) {
    CharsetEncoder enc = charset.newEncoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    CharBuffer in = CharBuffer.wrap(new char[]{c});
    ByteBuffer out = ByteBuffer.allocate(1);
    enc.encode(in, out, true);
    enc.flush(out);
    out.flip();
    if (out.hasRemaining()) {
      return out.get();
    }
    return 0x40; // EBCDIC space
  }

  /**
   * Translates an ASCII/Unicode string to an EBCDIC
   * byte array.
   *
   * @param s The string to encode.
   * @return The EBCDIC byte array.
   */
  public byte[] encodeString(String s) {
    byte[] result = new byte[s.length()];
    for (int i = 0; i < s.length(); i++) {
      result[i] = encode(s.charAt(i));
    }
    return result;
  }

  // ----- Static convenience methods (default Cp037) -----

  /**
   * Translates an EBCDIC byte to an ASCII character
   * using the default Cp037 codepage.
   *
   * @param b The EBCDIC byte.
   * @return The ASCII character.
   */
  public static char toAscii(byte b) {
    return DEFAULT.decode(b);
  }

  /**
   * Translates an ASCII character to an EBCDIC byte
   * using the default Cp037 codepage.
   *
   * @param c The ASCII character.
   * @return The EBCDIC byte.
   */
  public static byte toEbcdic(char c) {
    return DEFAULT.encode(c);
  }

  /**
   * Translates an ASCII string to an EBCDIC byte array
   * using the default Cp037 codepage.
   *
   * @param s The ASCII string.
   * @return The EBCDIC byte array.
   */
  public static byte[] toBytes(String s) {
    return DEFAULT.encodeString(s);
  }
}
