package no.jonathan.my_activities.activity;

import java.util.function.Function;

public class ActivityDtoMapper implements Function<Activity, ActivityDto> {

    @Override
    public ActivityDto apply(Activity activity) {
        return new ActivityDto(
                activity.getId(),
                activity.getDate(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getUser().getId()
        );
    }
}
