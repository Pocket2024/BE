package Project.Pocket.follow.exception;



public class FollowException extends RuntimeException {
    public FollowException() {
        super();
    }

    public FollowException(String message) {
        super(message);
    }

    public FollowException(String message, Throwable cause) {
        super(message, cause);
    }

    public FollowException(Throwable cause) {
        super(cause);
    }
}
