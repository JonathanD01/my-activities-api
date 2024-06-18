package no.jonathan.my_activities.activity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ActivityCreateRequest(
        @NotNull(message = "Date must be valid")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @NotEmpty(message = "Title cannot be empty")
        String title,

        @NotEmpty(message = "Description cannot be empty")
        String description
) {
}
