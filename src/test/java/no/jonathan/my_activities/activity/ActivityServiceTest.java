package no.jonathan.my_activities.activity;

import net.datafaker.Faker;
import no.jonathan.my_activities.exception.ActivityNoPermissionException;
import no.jonathan.my_activities.exception.ActivityNotFoundException;
import no.jonathan.my_activities.user.User;
import no.jonathan.my_activities.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quickperf.junit5.QuickPerfTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@QuickPerfTest
class ActivityServiceTest {

    private final Faker faker = new Faker();

    private final ActivityDtoMapper _activityDtoMapper = new ActivityDtoMapper();

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityDtoMapper activityDtoMapper;

    @InjectMocks
    private ActivityService underTest;

    private User user; // User with role USER
    private User guestUser; // User with role NONE
    private User adminUser; // User with role ADMIN

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(faker.random().nextLong())
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .enabled(true)
                .userRole(UserRole.USER)
                .build();

        guestUser = User.builder()
                .id(faker.random().nextLong())
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .enabled(true)
                .userRole(UserRole.NONE)
                .build();

        adminUser = User.builder()
                .id(faker.random().nextLong())
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .enabled(true)
                .userRole(UserRole.ADMIN)
                .build();
    }

    @Test
    @DisplayName("It should get all activities")
    void canGetAllActivities() {
        // Given
        var name = "john.doe@example.com";
        var mockAuthentication = mock(Authentication.class);
        var pageable = PageRequest.of(0, 10);

        // When
        when(mockAuthentication.getName()).thenReturn(name);

        // Then
        underTest.getAllActivities(mockAuthentication, pageable);

        verify(activityRepository, times(1))
                .findByUser_EmailAndDateGreaterThanEqualOrderByDateAsc(anyString(), any(), any());
    }

    @Test
    @DisplayName("It should create an activity")
    void canCreateActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var createRequest = new ActivityCreateRequest(
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(50, 100));

        var activity = Activity.builder()
                .date(createRequest.date())
                .title(createRequest.title())
                .description(createRequest.description())
                .user(user)
                .build();


        // When
        when(activityRepository.save(any())).thenReturn(activity);

        when(activityDtoMapper.apply(any())).thenReturn(_activityDtoMapper.apply(activity));

        // Then
        ActivityDto result = underTest.createActivity(mockAuthentication, createRequest);

        assertThat(result.date()).isEqualTo(createRequest.date());
        assertThat(result.title()).isEqualTo(createRequest.title());
        assertThat(result.description()).isEqualTo(createRequest.description());
    }

    @Test
    @DisplayName("It should update activity")
    void canUpdateActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var updateRequest = new ActivityUpdateRequest(
                faker.random().nextLong(),
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        var activity = Activity.builder()
                .date(updateRequest.date())
                .title(updateRequest.title())
                .description(updateRequest.description())
                .user(user)
                .build();

        // When
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        when(activityRepository.findById(updateRequest.id())).thenReturn(Optional.of(activity));

        when(activityRepository.save(any())).thenReturn(activity);

        when(activityDtoMapper.apply(any())).thenReturn(_activityDtoMapper.apply(activity));

        // Then
        ActivityDto result = underTest.updateActivity(mockAuthentication, updateRequest);

        assertThat(result.date()).isEqualTo(updateRequest.date());
        assertThat(result.title()).isEqualTo(updateRequest.title());
        assertThat(result.description()).isEqualTo(updateRequest.description());
    }

    @Test
    @DisplayName("It will throw when trying to update an activity that does not exist")
    void itWillThrowWhenActivityNotFoundWhenUpdateActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var updateRequest = new ActivityUpdateRequest(
                faker.random().nextLong(),
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        // When
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        when(activityRepository.findById(updateRequest.id())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.updateActivity(mockAuthentication, updateRequest))
                .isInstanceOf(ActivityNotFoundException.class)
                .hasMessage("Activity with id '" + updateRequest.id() + "' was not found");
    }

    @Test
    @DisplayName("It will throw if user does not have access to activity when updating activity")
    void itWillThrowIfUserDoesNotHaveAccessToActivityWhenUpdateActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var updateRequest = new ActivityUpdateRequest(
                faker.random().nextLong(),
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        var activity = Activity.builder()
                .date(updateRequest.date())
                .title(updateRequest.title())
                .description(updateRequest.description())
                .user(adminUser)
                .build();

        // When
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        when(activityRepository.findById(updateRequest.id())).thenReturn(Optional.of(activity));

        // Then
        assertThatThrownBy(() -> underTest.updateActivity(mockAuthentication, updateRequest))
                .isInstanceOf(ActivityNoPermissionException.class)
                .hasMessage("No permission");
    }

    @Test
    @DisplayName("It should allow an admin user to update another users activity")
    void canAdminUserUpdateAnotherUserActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var updateRequest = new ActivityUpdateRequest(
                faker.random().nextLong(),
                LocalDate.now().plusYears(faker.random().nextInt(5, 10)),
                faker.lorem().sentence(),
                faker.lorem().characters(100, 200));

        var activity = Activity.builder()
                .date(updateRequest.date())
                .title(updateRequest.title())
                .description(updateRequest.description())
                .user(user)
                .build();

        // When
        when(mockAuthentication.getPrincipal()).thenReturn(adminUser);

        when(activityRepository.findById(updateRequest.id())).thenReturn(Optional.of(activity));

        when(activityRepository.save(any())).thenReturn(activity);

        when(activityDtoMapper.apply(any())).thenReturn(_activityDtoMapper.apply(activity));

        // Then
        ActivityDto result = underTest.updateActivity(mockAuthentication, updateRequest);

        assertThat(result.date()).isEqualTo(updateRequest.date());
        assertThat(result.title()).isEqualTo(updateRequest.title());
        assertThat(result.description()).isEqualTo(updateRequest.description());
    }

    @Test
    @DisplayName("It should delete activity")
    void canDeleteActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var activityIdToDelete = faker.random().nextLong();

        var activity = mock(Activity.class);

        // When
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        when(activity.getUser()).thenReturn(user);

        when(activityRepository.findById(activityIdToDelete)).thenReturn(Optional.of(activity));

        when(activity.getId()).thenReturn(activityIdToDelete);

        // Then
        Long result = underTest.deleteActivity(mockAuthentication, activityIdToDelete);

        assertThat(result).isEqualTo(activityIdToDelete);

        verify(activityRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("It will throw when activity not found when deleting activity")
    void itWillThrowWhenActivityNotFoundWhenDeletingActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var activityIdToDelete = faker.random().nextLong();

        // When
        when(activityRepository.findById(activityIdToDelete)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.deleteActivity(mockAuthentication, activityIdToDelete))
                .isInstanceOf(ActivityNotFoundException.class)
                .hasMessage("Activity with id '" + activityIdToDelete + "' was not found");
    }

    @Test
    @DisplayName("It should allow an admin to delete another users activity")
    void canAdminUserDeleteOthersUsersActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var activityIdToDelete = faker.random().nextLong();

        var activity = mock(Activity.class);

        // When
        when(mockAuthentication.getPrincipal()).thenReturn(adminUser);

        when(activity.getUser()).thenReturn(user);

        when(activityRepository.findById(activityIdToDelete)).thenReturn(Optional.of(activity));

        when(activity.getId()).thenReturn(activityIdToDelete);

        // Then
        Long result = underTest.deleteActivity(mockAuthentication, activityIdToDelete);

        assertThat(result).isEqualTo(activityIdToDelete);

        verify(activityRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("It will throw if user does not have access to activity when deleting activity")
    void itWillThrowIfUserDoesNotHaveAccessToActivityWhenDeleteActivity() {
        // Given
        var mockAuthentication = mock(Authentication.class);

        var activityIdToDelete = faker.random().nextLong();

        var activity = mock(Activity.class);

        // When
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        when(activity.getUser()).thenReturn(adminUser);

        when(activityRepository.findById(activityIdToDelete)).thenReturn(Optional.of(activity));

        // Then
        assertThatThrownBy(() -> underTest.deleteActivity(mockAuthentication, activityIdToDelete))
                .isInstanceOf(ActivityNoPermissionException.class)
                .hasMessage("No permission");
    }
}