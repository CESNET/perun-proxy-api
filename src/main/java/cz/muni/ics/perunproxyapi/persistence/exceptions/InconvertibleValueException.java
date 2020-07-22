package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Runtime Exception thrown when requested convert to type that does not match the type of value we actually have.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class InconvertibleValueException extends RuntimeException {

    public InconvertibleValueException() {
        super();
    }

    public InconvertibleValueException(String s) {
        super(s);
    }

    public InconvertibleValueException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InconvertibleValueException(Throwable throwable) {
        super(throwable);
    }

    protected InconvertibleValueException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
