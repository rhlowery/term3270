package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for IBM 5250 protocol verification.
 */
public class Protocol5250Steps {

  private final TestContext context;

  /**
   * Constructs the step definitions with shared test context.
   *
   * @param context The shared context.
   */
  public Protocol5250Steps(TestContext context) {
    this.context = context;
  }

  /**
   * Initializes the screen buffer with specified dimensions.
   *
   * @param rows The number of rows.
   * @param cols The number of columns.
   */
  @Given("a screen buffer with {int} rows and {int} columns")
  public void a_screen_buffer_with_rows_and_columns(int rows, int cols) {
    context.getBuffer().resize(rows, cols);
  }

  /**
   * Initializes a 5250-specific data stream parser.
   */
  @Given("a 5250 data stream parser")
  public void a_5250_data_stream_parser() {
    context.setParser(new DataStreamParser5250(context.getBuffer()));
  }

  /**
   * Transitions the parser into data mode.
   */
  @Given("the 5250 parser is in data mode")
  public void the_5250_parser_is_in_data_mode() {
    context.getParser().startData();
  }

  /**
   * Sends hex-encoded 5250 data to the parser.
   *
   * @param hex The hex string.
   */
  @When("the host sends 5250 data {string}")
  public void the_host_sends_5250_data(String hex) {
    byte[] data = hexToBytes(hex);
    for (byte b : data) {
      context.getParser().processByte(b);
    }
  }

  /**
   * Sends a single character to the 5250 parser.
   *
   * @param s The character string.
   */
  @When("the host sends 5250 character {string}")
  public void the_host_sends_5250_character(String s) {
    char c = s.charAt(0);
    byte b = context.getConverter().encode(c);
    context.getParser().processByte(b);
  }

  /**
   * Verifies the length of the field at the current position.
   *
   * @param length The expected length.
   */
  @Then("the field should have length {int}")
  public void the_field_should_have_length(int length) {
    // To be implemented: 5250 fields store length differently
  }

  /**
   * Verifies that the cell at the specified coordinates is an attribute.
   *
   * @param row The row (1-indexed).
   * @param col The column (1-indexed).
   */
  @Then("the cell at row {int} column {int} should be an attribute")
  public void the_cell_at_row_column_should_be_an_attribute(int row, int col) {
    assertTrue(context.getBuffer().getCell(row, col).isAttribute(),
        "Cell at " + row + "," + col + " should be an attribute");
  }

  private byte[] hexToBytes(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                           + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }
}
