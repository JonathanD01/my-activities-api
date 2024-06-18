package no.jonathan.my_activities.token;

import java.time.LocalDateTime;

public record TokenDto(
        Long id,
        String token,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime validatedAt,
        Long userId
) {
}
