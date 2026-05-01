package com.rhlowery.term3270;

/**
 * Immutable configuration record for a terminal connection.
 * 
 * <p>This record encapsulates all parameters required to establish and 
 * maintain a session with a mainframe or midrange host, including network 
 * settings, protocol options, and localized character encoding.</p>
 * 
 * @param host           The hostname or IP address of the target server.
 * @param port           The TCP port number to connect to (typically 23 or 992).
 * @param terminalType   The IBM terminal model identifier (e.g., IBM-3278-2). 
 *                       Used by the host to determine screen dimensions and 
 *                       extended features.
 * @param secure         True if the connection should be established using 
 *                       SSL/TLS (TN3270S).
 * @param verifyHostname True if the server certificate's hostname must match 
 *                        the target host.
 * @param codepage       The EBCDIC codepage name (e.g., "Cp037", "Cp1140") 
 *                       for character translation.
 * @param emulationType  The base protocol to use, such as "3270" or "5250".
 */
public record ConnectionConfig(
    String host, 
    int port, 
    String terminalType, 
    boolean secure, 
    boolean verifyHostname,
    String codepage,
    String emulationType
) {
    /**
     * Factory method for creating a standard unencrypted connection configuration.
     *
     * @param host         The target host.
     * @param port         The target port.
     * @param terminalType The terminal type identifier.
     * @return A new ConnectionConfig with default security and encoding settings.
     */
    public static ConnectionConfig defaultConnection(
        String host, int port, String terminalType
    ) {
        return new ConnectionConfig(
            host, port, terminalType,
            false, false, EbcdicConverter.DEFAULT_CODEPAGE,
            "3270"
        );
    }

    /**
     * Calculates the number of rows based on the terminal type suffix.
     *
     * @return The row count (24, 32, 43, or 27).
     */
    public int rows() {
        if (terminalType != null) {
            if (terminalType.endsWith("-3")
                || terminalType.endsWith("-3-E")) {
                return 32;
            }
            if (terminalType.endsWith("-4")
                || terminalType.endsWith("-4-E")) {
                return 43;
            }
            if (terminalType.endsWith("-5")
                || terminalType.endsWith("-5-E")) {
                return 27;
            }
        }
        return 24; // Default Model 2
    }

    /**
     * Calculates the number of columns based on the terminal type suffix.
     *
     * @return The column count (80 or 132).
     */
    public int cols() {
        if (terminalType != null
            && (terminalType.endsWith("-5")
                || terminalType.endsWith("-5-E"))) {
            return 132;
        }
        return 80;
    }
}
