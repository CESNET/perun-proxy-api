package cz.muni.ics.perunproxyapi.persistence.exceptions;

public class RpcConnectionException extends Exception {

    public RpcConnectionException() {
        super();
    }

    public RpcConnectionException(String s) {
        super(s);
    }

    public RpcConnectionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RpcConnectionException(Throwable throwable) {
        super(throwable);
    }

    protected RpcConnectionException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

}
