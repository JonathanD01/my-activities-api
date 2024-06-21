package no.jonathan.my_activities.activity;

import jakarta.annotation.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ActivityUpdateRequest(
        @Nullable
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @Nullable
        String title,

        @Nullable
        String description
) {
}
