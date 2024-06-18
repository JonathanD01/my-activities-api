package no.jonathan.my_activities.exception;

public class UserAlreadyEnabledException extends RuntimeException {

    public UserAlreadyEnabledException() {
        super("User is already enabled!");
    }

}
