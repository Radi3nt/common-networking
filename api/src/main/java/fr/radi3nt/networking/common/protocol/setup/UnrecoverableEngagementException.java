package fr.radi3nt.networking.common.protocol.setup;

public class UnrecoverableEngagementException extends Exception {

    public UnrecoverableEngagementException() {
    }

    public UnrecoverableEngagementException(String message) {
        super(message);
    }

    public UnrecoverableEngagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrecoverableEngagementException(Throwable cause) {
        super(cause);
    }

    public UnrecoverableEngagementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
