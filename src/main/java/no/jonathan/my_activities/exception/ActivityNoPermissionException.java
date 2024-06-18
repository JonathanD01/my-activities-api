package no.jonathan.my_activities.exception;

public class ActivityNoPermissionException extends RuntimeException {

    public ActivityNoPermissionException() {
        super("No permission");
    }

}
