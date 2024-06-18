package no.jonathan.my_activities.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public record ResponseErrorDto(
        String message,
        @JsonProperty("http_status")
        HttpStatus httpStatus,
        ZonedDateTime timestamp
) {

}
