package io.github.tienquanutc.minify4j;

/**
 * Runtime exception thrown when loading or executing the WebAssembly minifier fails.
 */
public final class Minify4jException extends RuntimeException {
    /**
     * Creates an exception with a message.
     *
     * @param message error message
     */
    public Minify4jException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and cause.
     *
     * @param message error message
     * @param cause underlying cause
     */
    public Minify4jException(String message, Throwable cause) {
        super(message, cause);
    }
}
