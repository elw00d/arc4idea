package com.yandex.arcsupport;

/**
 * This exception is thrown during parsing of a Arc command output, in the case of unexpected output format.
 * The exception is unchecked: if it happens, it is either a format that we don't handle yet (and it should be fixed then), or an error
 * in a specific situation (which also should be handled).
 */
public class ArcFormatException extends RuntimeException {
    public ArcFormatException() {
    }

    public ArcFormatException(String message) {
        super(message);
    }

    public ArcFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
