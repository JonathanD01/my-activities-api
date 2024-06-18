package no.jonathan.my_activities.exception;

public class ActivityNotFoundException extends RuntimeException {

    public ActivityNotFoundException(Long id) {
        super("Activity with id '" + id + "' was not found");
    }


}
