package com.rhlowery.term3270;

/**
 * Centralized repository for IBM 3270 protocol constants.
 *
 * <p>This utility class contains the byte codes for commands
 * (sent by the host to initiate actions), orders (embedded in
 * data streams to control buffer positioning and attributes),
 * and structured field identifiers used for advanced features
 * like Query and File Transfer.</p>
 *
 * <p>Each command has two forms: a CCW (Channel Command Word)
 * form used by standard TN3270 and an SNA form used by
 * TN3270E. Both forms are handled by the parser.</p>
 */
public final class ProtocolConstants {

  private ProtocolConstants() {
    // Utility class
  }

  // ── 3270 Commands ─────────────────────────────────────
  // Each command has a CCW form and an SNA form.

  /** Write – CCW form. */
  public static final byte CMD_WRITE = (byte) 0xF1;
  /** Write – SNA form. */
  public static final byte CMD_WRITE_SNA = (byte) 0x01;

  /** Erase/Write – CCW form. */
  public static final byte CMD_ERASE_WRITE = (byte) 0xF5;
  /** Erase/Write – SNA form. */
  public static final byte CMD_ERASE_WRITE_SNA = (byte) 0x05;

  /** Erase/Write Alternate – CCW form. */
  public static final byte CMD_ERASE_WRITE_ALTERNATE = (byte) 0x7E;
  /** Erase/Write Alternate – SNA form. */
  public static final byte CMD_ERASE_WRITE_ALTERNATE_SNA = (byte) 0x0D;

  /** Erase All Unprotected – CCW form. */
  public static final byte CMD_ERASE_ALL_UNPROTECTED = (byte) 0x6F;
  /** Erase All Unprotected – SNA form. */
  public static final byte CMD_ERASE_ALL_UNPROTECTED_SNA = (byte) 0x0F;

  /** Write Structured Field – CCW form. */
  public static final byte CMD_WSF = (byte) 0xF3;
  /** Write Structured Field – SNA form. */
  public static final byte CMD_WSF_SNA = (byte) 0x11;
 
  /** Read Buffer – CCW form. */
  public static final byte CMD_READ_BUFFER = (byte) 0xF2;
  /** Read Buffer – SNA form. */
  public static final byte CMD_READ_BUFFER_SNA = (byte) 0x02;
 
  /** Read Modified – CCW form. */
  public static final byte CMD_READ_MODIFIED = (byte) 0xF6;
  /** Read Modified – SNA form. */
  public static final byte CMD_READ_MODIFIED_SNA = (byte) 0x06;
 
  /** Read Modified All – CCW form. */
  public static final byte CMD_READ_MODIFIED_ALL = (byte) 0x6E;
  /** Read Modified All – SNA form. */
  public static final byte CMD_READ_MODIFIED_ALL_SNA = (byte) 0x0E;

  // ── 3270 Orders ───────────────────────────────────────

  /** Set Buffer Address order. */
  public static final byte ORDER_SBA = (byte) 0x11;
  /** Start Field order. */
  public static final byte ORDER_SF  = (byte) 0x1D;
  /** Start Field Extended order. */
  public static final byte ORDER_SFE = (byte) 0x29;
  /** Set Attribute order. */
  public static final byte ORDER_SA  = (byte) 0x28;
  /** Repeat to Address order. */
  public static final byte ORDER_RA  = (byte) 0x3C;
  /** Erase Unprotected to Address order. */
  public static final byte ORDER_EUA = (byte) 0x12;
  /** Insert Cursor order. */
  public static final byte ORDER_IC  = (byte) 0x13;
  /** Program Tab order. */
  public static final byte ORDER_PT  = (byte) 0x05;

  // ── Structured Field types (inside WSF) ───────────────

  /** SF type: Read Partition. */
  public static final byte SF_READ_PARTITION = (byte) 0x01;
  /** SF type: Query Reply. */
  public static final byte SF_TYPE_QUERY_REPLY = (byte) 0x81;
  /** SF type: File Transfer (IND$FILE) Data. */
  public static final byte SF_TYPE_FILE_TRANSFER = (byte) 0x47;
  /** SF type: File Transfer Query Code. */
  public static final byte SF_QCODE_FILE_TRANSFER = (byte) 0xCA;

  // ── Read Partition sub-types ──────────────────────────

  /** Read Partition mode: Query. */
  public static final byte RP_QUERY = (byte) 0x02;
  /** Read Partition mode: Query List. */
  public static final byte RP_QUERY_LIST = (byte) 0x03;
  /** Partition ID meaning "all partitions". */
  public static final byte RP_PID_ALL = (byte) 0xFF;

  // ── AIDs ──────────────────────────────────────────────

  /** Attention Identifier for Structured Fields. */
  public static final byte AID_STRUCTURED_FIELD = (byte) 0x88;
}
