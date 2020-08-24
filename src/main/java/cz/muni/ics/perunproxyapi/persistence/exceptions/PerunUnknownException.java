package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Wrapper around different exceptions thrown by Perun. Usually means we cannot take any meaningful action to fix
 * the cause.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public class PerunUnknownException extends Exception {

    public PerunUnknownException() {
        super();
    }

    public PerunUnknownException(String s) {
        super(s);
    }

    public PerunUnknownException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PerunUnknownException(Throwable throwable) {
        super(throwable);
    }

    protected PerunUnknownException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

}
