package no.jonathan.my_activities.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;


public record RegistrationRequest (
        @NotEmpty(message = "Firstname can not be empty")
        String firstname,

        @NotEmpty(message = "Lastname can not be empty")
        String lastname,

        @Email(message = "Email must be a valid email address")
        @NotEmpty(message = "Email can not be empty") String email,

        @NotEmpty(message = "Password can not be empty")
        @Size(min = 8, message = "Password should be 8 characters long minimum")
        String password
){
}
