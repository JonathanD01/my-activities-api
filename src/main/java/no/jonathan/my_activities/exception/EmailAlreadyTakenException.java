package no.jonathan.my_activities.exception;

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("Email " + email + " is already taken");
    }

}