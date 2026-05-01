package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.nio.charset.UnsupportedCharsetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for multi-codepage EBCDIC
 * translation scenarios.
 */
public class CodepageSteps {

  private EbcdicConverter converter;
  private byte encodedByte;
  private char decodedChar;
  private Exception caughtException;

  /**
   * Initializes an EBCDIC converter with the specified codepage.
   *
   * @param codepage The EBCDIC codepage identifier.
   */
  @Given("an EBCDIC converter using codepage {string}")
  public void anEbcdicConverterUsingCodepage(
      String codepage) {
    converter = new EbcdicConverter(codepage);
  }

  /**
   * Initializes a default EBCDIC converter.
   */
  @Given("a default EBCDIC converter")
  public void aDefaultEbcdicConverter() {
    converter = EbcdicConverter.defaultConverter();
  }

  /**
   * Encodes a character using the current converter.
   *
   * @param ch The character to encode.
   */
  @When("I encode the character {string}")
  public void iEncodeTheCharacter(String ch) {
    encodedByte = converter.encode(ch.charAt(0));
  }

  /**
   * Decodes the previously encoded byte.
   */
  @When("I decode the result")
  public void iDecodeTheResult() {
    decodedChar = converter.decode(encodedByte);
  }

  /**
   * Attempts to create a converter with the specified codepage.
   *
   * @param codepage The codepage identifier.
   */
  @When("I create a converter with codepage {string}")
  public void iCreateAConverterWithCodepage(
      String codepage) {
    try {
      new EbcdicConverter(codepage);
    } catch (UnsupportedCharsetException e) {
      caughtException = e;
    }
  }

  /**
   * Verifies the decoded character matches the expected value.
   *
   * @param expected The expected character.
   */
  @Then("the decoded character should be {string}")
  public void theDecodedCharacterShouldBe(
      String expected) {
    assertEquals(
        expected.charAt(0), decodedChar,
        "Round-trip decode failed");
  }

  /**
   * Verifies the converter's current codepage.
   *
   * @param expected The expected codepage identifier.
   */
  @Then("the codepage should be {string}")
  public void theCodepageShouldBe(String expected) {
    assertNotNull(converter);
    assertEquals(expected, converter.getCodepage());
  }

  /**
   * Verifies that an UnsupportedCharsetException was thrown.
   */
  @Then("an UnsupportedCharsetException should be thrown")
  public void anExceptionShouldBeThrown() {
    assertNotNull(
        caughtException,
        "Expected UnsupportedCharsetException");
    assertTrue(
        caughtException
            instanceof UnsupportedCharsetException);
  }
}
