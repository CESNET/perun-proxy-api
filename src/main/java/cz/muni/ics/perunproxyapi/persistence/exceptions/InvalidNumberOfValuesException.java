package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Runtime Exception thrown when more than one value is returned for unique attribute.
 *
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
public class InvalidNumberOfValuesException extends RuntimeException {
    public InvalidNumberOfValuesException() {
    }

    public InvalidNumberOfValuesException(String s) {
        super(s);
    }

    public InvalidNumberOfValuesException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidNumberOfValuesException(Throwable throwable) {
        super(throwable);
    }

    public InvalidNumberOfValuesException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

}
