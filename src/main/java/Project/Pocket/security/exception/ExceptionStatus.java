package Project.Pocket.security.exception;

public enum ExceptionStatus {

    WRONG_EMAIL("Wrong email"),
    WRONG_PASSWORD("Wrong password"),

     AUTHENTICATION("Authentication failed"),

    DUPLICATED_NICKNAME("Nickname is duplicated"),
    DUPLICATED_EMAIL("Email is duplicated"),
    DUPLICATED_PHONENUMBER("Phonenumber is duplicated");

    private final String message;

    ExceptionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
