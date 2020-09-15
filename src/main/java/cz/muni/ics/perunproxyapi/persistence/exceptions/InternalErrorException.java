package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Represents some error we cannot solve, nor can we give useful output.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class InternalErrorException extends RuntimeException {

    public InternalErrorException() {
        super();
    }

    public InternalErrorException(String message) {
        super(message);
    }

    public InternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalErrorException(Throwable cause) {
        super(cause);
    }

    protected InternalErrorException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
