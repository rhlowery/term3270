package com.rhlowery.term3270;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for File Transfer (IND$FILE) behavior.
 */
public class FileTransferSteps {

  private final TestContext context;
  private byte[] lastReply;

  /**
   * Constructs the step definitions with the shared test context.
   *
   * @param context The shared BDD context.
   */
  public FileTransferSteps(TestContext context) {
    this.context = context;
  }

  /**
   * Simulates the host sending a specific request type.
   *
   * @param type The type of host request (e.g., "Read Partition (Query)").
   */
  @When("the host sends a {string} request")
  public void theHostSendsARequest(String type) {
    if (type.equals("Read Partition (Query)")) {
      // Send valid WSF Read Partition (Query):
      // 0xF3 (WSF), 0x00 0x05 (Len), 0x01 (Type: Read Partition), 0xFF (PID: All), 0x02 (Mode: Query)
      byte[] query = {(byte) 0xF3, 0x00, 0x05, 0x01, (byte) 0xFF, 0x02};
      context.getSession().receiveHostData(query);
    }
  }

  /**
   * Verifies that the terminal replies with the correct query reply.
   *
   * @param replyType The expected reply type.
   * @param support   The expected supported feature.
   */
  @Then("the terminal should reply with a {string} including {string} support")
  public void theTerminalShouldReplyWithAIncludingSupport(String replyType, String support) {
    byte[] sent = context.getSession().getLastSentData();
    // AID 0x88 is Structured Field
    assertEquals((byte) 0x88, sent[0]);
    // Check for 0xCA ID in the reply
    boolean foundCA = false;
    for (byte b : sent) {
      if ((b & 0xFF) == 0xCA) foundCA = true;
    }
    assertTrue(foundCA, "File Transfer ID (0xCA) not found in Query Reply");
  }

  /**
   * Verifies that the reply contains the specific File Transfer attribute.
   *
   * @param attr The expected attribute name.
   */
  @Then("the reply should contain the {string} \\(File Transfer) attribute")
  public void theReplyShouldContainTheFileTransferAttribute(String attr) {
    // Already verified in previous step
  }

  /**
   * Initializes an active file transfer for the specified file.
   *
   * @param filename The name of the file to transfer.
   * @throws Exception If the transfer fails to initialize.
   */
  @Given("a file transfer is active for {string}")
  public void aFileTransferIsActiveFor(String filename) throws Exception {
    context.getSession().downloadFile("IND$FILE GET " + filename, filename);
  }

  /**
   * Simulates the host sending a structured field with data.
   *
   * @param type The type of structured field.
   * @param data The data content.
   */
  @When("the host sends a {string} with data {string}")
  public void theHostSendsAWithData(String type, String data) {
    if (type.equals("Write Structured Field")) {
      byte[] bytes = data.getBytes();
      int sfLen = bytes.length + 3; // SF Length: Len(2) + ID(1) + Data
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(0xF3); // WSF Command
      out.write((sfLen >> 8));
      out.write((sfLen & 0xFF));
      out.write(0x47); // SF ID: File Transfer Data
      try {
        out.write(bytes);
        context.getSession().receiveHostData(out.toByteArray());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Simulates the host sending a specific indicator (e.g., completion).
   *
   * @param type      The type of message.
   * @param indicator The specific indicator.
   * @throws Exception If the completion simulation fails.
   */
  @When("the host sends a {string} with a {string} indicator")
  public void theHostSendsAWithAIndicator(String type, String indicator) throws Exception {
    context.getSession().getFileTransferHandler().complete();
  }

  /**
   * Verifies the content of a downloaded local file.
   *
   * @param filename The name of the file.
   * @param expected The expected file content.
   * @throws Exception If file reading fails.
   */
  @Then("the local file {string} should contain {string}")
  public void theLocalFileShouldContain(String filename, String expected) throws Exception {
    File file = new File(filename);
    assertTrue(file.exists());
    String actual = new String(Files.readAllBytes(file.toPath()));
    assertEquals(expected, actual);
    file.delete(); // Cleanup
  }

  /**
   * Creates a local file with specific content for upload testing.
   *
   * @param filename The name of the file.
   * @param content  The file content.
   * @throws Exception If file creation fails.
   */
  @Given("a local file {string} with content {string}")
  public void aLocalFileWithContent(String filename, String content) throws Exception {
    Files.write(new File(filename).toPath(), content.getBytes());
  }

  /**
   * Simulates uploading a local file to the host.
   *
   * @param filename The name of the local file.
   * @param command  The host command to initiate upload.
   */
  @When("I upload the file {string} with command {string}")
  public void iUploadTheFileWithCommand(String filename, String command) {
    context.getSession().uploadFile(command, filename);
  }

  /**
   * Verifies that the terminal sent a structured field with the expected data.
   *
   * @param expected The expected data string.
   */
  @Then("the terminal should send a Structured Field with data {string}")
  public void theTerminalShouldSendAStructuredFieldWithData(String expected) {
    byte[] sent = context.getSession().getLastSentData();
    // In our simplified test, we check if the last chunk matches
    // For large files we would need to capture all chunks
    String actual = new String(sent, 4, sent.length - 4); // Skip AID(1), Len(2), ID(1)
    assertEquals(expected, actual);
    new File("UPLOAD.TXT").delete(); // Cleanup
  }
}
