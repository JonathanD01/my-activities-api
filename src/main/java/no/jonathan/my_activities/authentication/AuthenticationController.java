package no.jonathan.my_activities.authentication;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.jonathan.my_activities.response.Response;
import no.jonathan.my_activities.response.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final ResponseUtil responseUtil;

    @PostMapping("register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> registerAccount(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("authenticate")
    public ResponseEntity<Response<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(responseUtil.buildSuccessResponse(service.authenticate(request)));
    }
    @PostMapping("activate-account")
    public ResponseEntity<?> activateAccount(
            @RequestBody @Valid ActivateAccountRequest request
    ) throws MessagingException {
        var success = service.activateAccount(request.token());

        // Occurs if the token to be activated have expired
        if (!success) {
            service.sendNewToken(request.token());
            return ResponseEntity.ok(responseUtil.buildFailedResponse(
                    "Activation token has expired. A new token has been send to the same email address"));
        }

        return ResponseEntity.accepted().build();
    }


}
