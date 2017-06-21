package tv.lycam.server.api.exception;

/**
 * Created by lycamandroid on 2017/6/21.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}