package com.rhlowery.term3270;
import com.rhlowery.term3270.ui.*;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.ServerSocket;
import org.openide.util.Lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step definitions for Network behavior.
 */
public class NetworkSteps {

  private ServerSocket mockServer;
  private int port;
  private ITerminalSession session;

  /**
   * Initializes a mock mainframe server for network testing.
   *
   * @throws IOException If the mock server fails to start.
   */
  @Given("a mock mainframe server")
  public void aMockMainframeServer() throws IOException {
    mockServer = new ServerSocket(0); // Random free port
    port = mockServer.getLocalPort();
    new Thread(() -> {
      try {
        mockServer.accept();
      } catch (IOException ignored) {}
    }).start();
  }

  /**
   * Initiates a connection from the terminal to the mock server.
   */
  @When("I initiate a connection to the mock server")
  public void iInitiateAConnectionToTheMockServer() {
    session = Lookup.getDefault().lookup(ITerminalSession.class);
    session.connect(ConnectionConfig.defaultConnection("localhost", port, "IBM-3278-2"));
  }

  /**
   * Verifies that the terminal can successfully disconnect from the server.
   *
   * @throws IOException If the mock server fails to close.
   */
  @Then("I should be able to disconnect")
  public void iShouldBeAbleToDisconnect() throws IOException {
    session.disconnect();
    assertEquals("DISCONNECTED", session.getStatus());
    mockServer.close();
  }
}
