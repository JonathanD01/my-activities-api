package no.jonathan.my_activities.authentication;

import jakarta.validation.constraints.NotEmpty;

public record ActivateAccountRequest (
        @NotEmpty(message = "Token cannot be empty")
        String token
) {
}
