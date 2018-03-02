package nl.quintor.studybits.indy.wrapper.exception;

public class IndyWrapperException extends RuntimeException {
    public IndyWrapperException() {
        super();
    }

    public IndyWrapperException(String message) {
        super(message);
    }

    public IndyWrapperException(String message, Throwable cause) {
        super(message, cause);
    }
}
