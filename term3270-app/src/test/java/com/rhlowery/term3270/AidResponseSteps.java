package com.rhlowery.term3270;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for AID Response behavior.
 */
public class AidResponseSteps {

  private final TestContext context;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public AidResponseSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Verifies that the outbound data stream contains the expected AID code.
   *
   * @param aidName The name of the expected AID key.
   */
  @Then("the outbound data should contain AID {string}")
  public void theOutboundDataShouldContainAid(String aidName) {
    byte[] data = context.getSession().getLastSentData();
    assertNotNull(data);
    assertTrue(data.length > 0);
    assertEquals(AIDKey.valueOf(aidName).getCode(), data[0]);
  }

  /**
   * Verifies that the outbound data stream contains the expected cursor address.
   *
   * @param row The expected row (1-indexed).
   * @param col The expected column (1-indexed).
   */
  @Then("the outbound data should contain cursor address at row {int} column {int}")
  public void theOutboundDataShouldContainCursorAddressAtRowColumn(Integer row, Integer col) {
    byte[] data = context.getSession().getLastSentData();
    int expectedAddr = (row - 1) * 80 + (col - 1);
    byte[] expectedBytes = AddressConverter.fromAddress(expectedAddr);
    
    assertEquals(expectedBytes[0], data[1]);
    assertEquals(expectedBytes[1], data[2]);
  }

  /**
   * Verifies that the outbound data stream contains the expected text for a field.
   *
   * @param expectedText The text expected in the stream.
   * @param row          The field's row (1-indexed).
   * @param col          The field's column (1-indexed).
   */
  @Then("the outbound data should contain {string} for field at row {int} column {int}")
  public void theOutboundDataShouldContainForFieldAtRowColumn(String expectedText, Integer row, Integer col) {
    byte[] data = context.getSession().getLastSentData();
    // The SBA address for a field is the address of the first character, 
    // which is one position after the attribute character.
    int fieldAddr = (row - 1) * 80 + (col - 1) + 1;
    byte[] addrBytes = AddressConverter.fromAddress(fieldAddr);
    
    // Search for 0x11 <addr1> <addr2> <text>
    boolean found = false;
    for (int i = 3; i <= data.length - expectedText.length() - 3; i++) {
      if (data[i] == 0x11 && data[i+1] == addrBytes[0] && data[i+2] == addrBytes[1]) {
        byte[] expectedEbcdic = EbcdicConverter.toBytes(expectedText);
        for (int j = 0; j < expectedEbcdic.length; j++) {
          assertEquals(expectedEbcdic[j], data[i + 3 + j]);
        }
        found = true;
        break;
      }
    }
    assertTrue(found, "Field data not found in outbound stream");
  }

  /**
   * Verifies that the Modified Data Tag (MDT) is set for a field.
   *
   * @param row The field's row (1-indexed).
   * @param col The field's column (1-indexed).
   */
  @Then("the field at row {int} column {int} should be modified")
  public void theFieldAtRowColumnShouldBeModified(Integer row, Integer col) {
    ScreenCell cell = context.getBuffer().getCell(row, col);
    assertTrue(cell.getAttribute().isModified());
  }

  /**
   * Verifies that the Modified Data Tag (MDT) is NOT set for a field.
   *
   * @param row The field's row (1-indexed).
   * @param col The field's column (1-indexed).
   */
  @Then("the field at row {int} column {int} should not be modified")
  public void theFieldAtRowColumnShouldNotBeModified(Integer row, Integer col) {
    ScreenCell cell = context.getBuffer().getCell(row, col);
    assertFalse(cell.getAttribute().isModified());
  }

  /**
   * Simulates parsing a Write Control Character (WCC) with the reset MDT bit set.
   */
  @When("I parse a WCC with reset MDT bit set")
  public void iParseAWccWithResetMdtBitSet() {
    // WCC follows a Write command. Bit 1 (0x01) is reset MDT.
    context.getParser().reset();
    context.getParser().processByte((byte) 0xF1); // W command
    context.getParser().processByte((byte) 0x01); // WCC with bit 1 set
  }
}
