package ro.alinvasile.projects.cache.io;

public class InvalidDatabaseFileException extends Exception {

    public InvalidDatabaseFileException() {
        super();
    }

    public InvalidDatabaseFileException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvalidDatabaseFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDatabaseFileException(String message) {
        super(message);
    }

    public InvalidDatabaseFileException(Throwable cause) {
        super(cause);
    }

}
