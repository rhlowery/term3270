package com.rhlowery.term3270;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for EBCDIC translation.
 */
public class EbcdicSteps {

  private char translatedChar;

  /**
   * Translates an EBCDIC byte to its ASCII character equivalent.
   *
   * @param ebcdicByte The EBCDIC byte value.
   */
  @When("I translate EBCDIC byte {int}")
  public void iTranslateEbcdicByte(Integer ebcdicByte) {
    translatedChar = EbcdicConverter.toAscii(ebcdicByte.byteValue());
  }

  /**
   * Verifies that the translated character matches the expected ASCII value.
   *
   * @param expected The expected ASCII character string.
   */
  @Then("the ASCII character should be {string}")
  public void theAsciiCharacterShouldBe(String expected) {
    assertEquals(expected.charAt(0), translatedChar);
  }
}
