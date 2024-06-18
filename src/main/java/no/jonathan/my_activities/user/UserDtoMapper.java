package no.jonathan.my_activities.user;

import java.util.function.Function;

public class UserDtoMapper implements Function<User, UserDto> {

    @Override
    public UserDto apply(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.isAccountLocked(),
                user.isEnabled(),
                user.getCreatedDate(),
                user.getLastModifiedDate(),
                user.getUserRole().name()
        );
    }
}
