package cz.muni.ics.perunproxyapi.persistence.exceptions;

/**
 * Represents an error when reading a file was not successful.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public class MissingOrInvalidFileException extends Exception {

    public MissingOrInvalidFileException() {
        super();
    }

    public MissingOrInvalidFileException(String s) {
        super(s);
    }

    public MissingOrInvalidFileException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MissingOrInvalidFileException(Throwable throwable) {
        super(throwable);
    }

    protected MissingOrInvalidFileException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

    }
