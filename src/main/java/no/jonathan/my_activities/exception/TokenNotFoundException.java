package no.jonathan.my_activities.exception;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException(String token) {
        super("Token " + token + " was not found");
    }

}
