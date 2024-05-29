package Project.Pocket.security.exception;

public class CustomException extends RuntimeException {

    private ExceptionStatus status;
    public CustomException(ExceptionStatus status) {
        super(status.getMessage());
        this.status = status;
    }

    public ExceptionStatus getStatus() {
        return status;
    }
}
