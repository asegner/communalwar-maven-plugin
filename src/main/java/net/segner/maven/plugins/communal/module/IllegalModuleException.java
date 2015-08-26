package net.segner.maven.plugins.communal.module;

/**
 * Signals that an application module exception of some sort has occurred.
 */
public class IllegalModuleException extends Exception {

    private static final long serialVersionUID = -8120279032807192989L;

    /**
     * Constructs an {@code IllegalModuleException} with {@code null}
     * as its error detail message.
     */
    public IllegalModuleException() {
        super();
    }

    /**
     * Constructs an {@code IllegalModuleException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     */
    public IllegalModuleException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code IllegalModuleException} with the specified detail message
     * and cause.
     * <p>
     * <p> Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A null value is permitted,
     *                and indicates that the cause is nonexistent or unknown.)
     */
    public IllegalModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code IllegalModuleException} with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     * This constructor is useful for module exceptions that are little more
     * than wrappers for other throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A null value is permitted,
     *              and indicates that the cause is nonexistent or unknown.)
     */
    public IllegalModuleException(Throwable cause) {
        super(cause);
    }
}
