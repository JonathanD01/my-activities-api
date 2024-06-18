package no.jonathan.my_activities.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Converts a LocalDate to a string in the format yyyy-MM-dd.
     *
     * @param date the LocalDate to be converted
     * @return a string representation of the LocalDate in the format yyyy-MM-dd
     */
    public static String format(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return date.format(DATE_FORMATTER);
    }
}
