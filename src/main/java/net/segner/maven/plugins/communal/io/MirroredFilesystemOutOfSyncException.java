package net.segner.maven.plugins.communal.io;

import java.io.IOException;

public class MirroredFilesystemOutOfSyncException extends IOException {

    /**
     * Constructs an {@code MirroredFilesystemOutOfSyncException} with {@code null}
     * as its error detail message.
     */
    public MirroredFilesystemOutOfSyncException() {
        super();
    }

    /**
     * Constructs an {@code MirroredFilesystemOutOfSyncException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     */
    public MirroredFilesystemOutOfSyncException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code MirroredFilesystemOutOfSyncException} with the specified detail message
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
    public MirroredFilesystemOutOfSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code MirroredFilesystemOutOfSyncException} with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     * This constructor is useful for mirrored filesystem exceptions that are little more
     * than wrappers for other throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A null value is permitted,
     *              and indicates that the cause is nonexistent or unknown.)
     */
    public MirroredFilesystemOutOfSyncException(Throwable cause) {
        super(cause);
    }
}
