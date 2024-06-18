package no.jonathan.my_activities.activity;

import java.time.LocalDate;

public record ActivityDto(
        Long id,
        LocalDate date,
        String title,
        String description,
        Long userId
) {

}
