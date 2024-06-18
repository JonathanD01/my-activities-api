package no.jonathan.my_activities.user;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String firstname,
        String lastname,
        String email,
        boolean accountLocked,
        boolean enabled,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,
        String userRole
) {
}
