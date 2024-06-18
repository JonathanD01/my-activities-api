package no.jonathan.my_activities.activity;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ActivityUpdateRequest(
        @NotNull(message = "Id cannot be null")
        Long id,

        @Nullable
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @Nullable
        String title,

        @Nullable
        String description
) {
}
