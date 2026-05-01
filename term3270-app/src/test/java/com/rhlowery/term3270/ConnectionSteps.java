package com.rhlowery.term3270;
import com.rhlowery.term3270.ui.*;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.ServerSocket;
import org.openide.util.Lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Connection behavior using Lookup for implementation.
 */
public class ConnectionSteps {

  private String host;
  private int port;
  private String termType;
  private ITerminalSession session;

  /**
   * Defines a target mainframe host and port, starting a dummy server for testing.
   *
   * @param host          The target host address.
   * @param requestedPort The port to listen on.
   * @throws IOException If the dummy server fails to start.
   */
  @Given("a mainframe host {string} on port {int}")
  public void aMainframeHostOnPort(String host, Integer requestedPort) throws IOException {
    // Ignore the requested port and use a random free one for the test
    ServerSocket ss = new ServerSocket(0);
    this.port = ss.getLocalPort();
    new Thread(() -> {
      try {
        ss.accept();
        ss.close();
      } catch (IOException ignored) {}
    }).start();
    this.host = "localhost";
  }

  /**
   * Defines the terminal type to be used for the connection.
   *
   * @param termType The IBM terminal type string.
   */
  @Given("a terminal type of {string}")
  public void aTerminalTypeOf(String termType) {
    this.termType = termType;
  }

  /**
   * Attempts to establish a connection to the host.
   */
  @When("I attempt to connect")
  public void iAttemptToConnect() {
    session = Lookup.getDefault().lookup(ITerminalSession.class);
    assertNotNull(session, "No ITerminalSession implementation found!");
    session.connect(ConnectionConfig.defaultConnection(host, port, termType));
  }

  /**
   * Verifies that the terminal screen has been successfully initialized.
   */
  @Then("the terminal screen should be initialized")
  public void theTerminalScreenShouldBeInitialized() {
    assertTrue(session.isScreenInitialized());
  }
}
