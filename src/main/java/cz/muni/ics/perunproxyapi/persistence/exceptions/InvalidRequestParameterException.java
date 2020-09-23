package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Exception should be thrown at controller level when passed request parameters do not meet criteria.
 *
 * @author Dominik Frantisek Bucik
 */
public class InvalidRequestParameterException extends Exception {

    public InvalidRequestParameterException() {
        super();
    }

    public InvalidRequestParameterException(String message) {
        super(message);
    }

    public InvalidRequestParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRequestParameterException(Throwable cause) {
        super(cause);
    }

    protected InvalidRequestParameterException(String message, Throwable cause, boolean enableSuppression,
                                               boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
