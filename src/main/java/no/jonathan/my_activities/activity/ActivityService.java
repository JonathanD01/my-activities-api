package no.jonathan.my_activities.activity;

import lombok.RequiredArgsConstructor;
import no.jonathan.my_activities.exception.ActivityNoPermissionException;
import no.jonathan.my_activities.exception.ActivityNotFoundException;
import no.jonathan.my_activities.user.User;
import no.jonathan.my_activities.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDtoMapper activityDtoMapper;

    public Page<ActivityDto> getAllActivities(
            Authentication authentication,
            Pageable pageable
    ) {
        return activityRepository
                .findByUser_EmailAndDateGreaterThanEqualOrderByDateAsc(authentication.getName(), LocalDate.now(), pageable);
    }

    public ActivityDto createActivity(Authentication authentication, ActivityCreateRequest createRequest) {
        User user = ((User) authentication.getPrincipal());

        Activity newActivity = Activity.builder()
                .date(createRequest.date())
                .title(createRequest.title())
                .description(createRequest.description())
                .user(user)
                .build();

        newActivity = activityRepository.save(newActivity);

        return activityDtoMapper.apply(newActivity);
    }

    public ActivityDto updateActivity(Authentication authentication, ActivityUpdateRequest updateRequest) {
        User user = ((User) authentication.getPrincipal());

        Long activityId = updateRequest.id();

        Activity activityToUpdate = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(activityId));

        hasAccessToActivity(user, activityToUpdate);

        if (updateRequest.date() != null) {
            activityToUpdate.setDate(updateRequest.date());
        }

        if (updateRequest.title() != null) {
            activityToUpdate.setTitle(updateRequest.title());
        }

        if (updateRequest.description() != null) {
            activityToUpdate.setDescription(updateRequest.description());
        }

        var updatedActivity = activityRepository.save(activityToUpdate);

        return activityDtoMapper.apply(updatedActivity);
    }

    public Long deleteActivity(Authentication authentication, Long id) {
        User user = ((User) authentication.getPrincipal());

        Activity activityToDelete = activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));

        hasAccessToActivity(user, activityToDelete);

        activityRepository.delete(activityToDelete);

        return activityToDelete.getId();
    }

    /**
     * Check if user have access to the activity
     * @param user is the user who makes the request
     * @param activity is the activity to be accessed
     * @throws ActivityNoPermissionException is thrown if it fails
     */
    private void hasAccessToActivity(
            User user,
            Activity activity
    ) {
        boolean isUserOwnerOfActivity = Objects.equals(activity.getUser().getId(), user.getId());
        boolean isUserAdmin = user.getUserRole().equals(UserRole.ADMIN);
        if (!isUserOwnerOfActivity && !isUserAdmin) {
            throw new ActivityNoPermissionException();
        }
    }
}
