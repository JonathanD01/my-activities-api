package no.jonathan.my_activities.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record Response<T>(
        @JsonProperty("response") ResponseType type,
        @JsonProperty("errors") List<ResponseErrorDto> errors,
        @JsonProperty("result") T result) {
}