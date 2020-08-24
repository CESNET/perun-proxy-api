package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Represents an error when obtaining connection to one of the Perun interfaces (LDAP/RPC).
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public class PerunConnectionException extends Exception {

    public PerunConnectionException() {
        super();
    }

    public PerunConnectionException(String s) {
        super(s);
    }

    public PerunConnectionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PerunConnectionException(Throwable throwable) {
        super(throwable);
    }

    protected PerunConnectionException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

}
