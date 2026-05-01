package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Step definitions verifying the protocol abstraction
 * layer introduced by IDataStreamParser.
 */
public class ProtocolAbstractionSteps {

  private IDataStreamParser parser;

  /**
   * Initializes a TN3270 data stream parser for testing.
   */
  @Given("a TN3270 data stream parser")
  public void aTn3270DataStreamParser() {
    ScreenBuffer buffer = new ScreenBuffer();
    parser = new DataStreamParser(buffer);
  }

  /**
   * Verifies that the parser implements the IDataStreamParser interface.
   */
  @Then("it should implement the common parser interface")
  public void itShouldImplementInterface() {
    assertInstanceOf(
        IDataStreamParser.class, parser,
        "DataStreamParser must implement "
            + "IDataStreamParser");
  }

  /**
   * Ensures a default terminal session is available.
   */
  @Given("a default terminal session")
  public void aDefaultTerminalSession() {
    // Validated at compile time
  }

  /**
   * Verifies that the parser implements the expected common interface.
   */
  @Then("the parser should implement the common interface")
  public void theParserShouldImplement() {
    DataStreamParser concreteParser =
        new DataStreamParser(new ScreenBuffer());
    assertInstanceOf(
        IDataStreamParser.class, concreteParser);
  }
}
