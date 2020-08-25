package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Represents error in LDAP lookup.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class LookupException extends Exception {

    public LookupException() {
        super();
    }

    public LookupException(String message) {
        super(message);
    }

    public LookupException(String message, Throwable cause) {
        super(message, cause);
    }

    public LookupException(Throwable cause) {
        super(cause);
    }

    protected LookupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
