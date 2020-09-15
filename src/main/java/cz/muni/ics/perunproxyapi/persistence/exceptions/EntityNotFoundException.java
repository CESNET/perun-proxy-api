package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Exception represents that the requested entity has not been found.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class EntityNotFoundException extends Exception {

    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }

    protected EntityNotFoundException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
