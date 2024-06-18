package no.jonathan.my_activities.exception;

import lombok.RequiredArgsConstructor;
import no.jonathan.my_activities.response.Response;
import no.jonathan.my_activities.response.ResponseErrorDto;
import no.jonathan.my_activities.response.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalExceptionHandler {

    private final ResponseUtil responseUtil;

    @ExceptionHandler(value = EmailAlreadyTakenException.class)
    public ResponseEntity<Response<ResponseErrorDto>> handleEmailAlreadyTakenException(
            EmailAlreadyTakenException exception
    ) {
        ResponseErrorDto errorDTO = responseUtil.createAPIErrorDTO(exception);
        Response<ResponseErrorDto> response = responseUtil.createAPIResponse(errorDTO);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = TokenNotFoundException.class)
    public ResponseEntity<Response<ResponseErrorDto>> handleTokenNotFoundException(
            TokenNotFoundException exception
    ) {
        ResponseErrorDto errorDTO = responseUtil.createAPIErrorDTO(exception);
        Response<ResponseErrorDto> response = responseUtil.createAPIResponse(errorDTO);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<Response<ResponseErrorDto>> handleBindException(
            BindException exception
    ) {
        List<ResponseErrorDto> errorDTOList = responseUtil.createAPIErrorDTOsForBindException(exception);
        Response<ResponseErrorDto> response = responseUtil.createAPIResponse(errorDTOList);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = UserAlreadyEnabledException.class)
    public ResponseEntity<Response<ResponseErrorDto>> handleUserAlreadyEnabledException(
            UserAlreadyEnabledException exception
    ) {
        ResponseErrorDto errorDTO = responseUtil.createAPIErrorDTO(exception);
        Response<ResponseErrorDto> response = responseUtil.createAPIResponse(errorDTO);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = ActivityNoPermissionException.class)
    public ResponseEntity<Response<ResponseErrorDto>> handleActivityNoPermissionException(
            ActivityNoPermissionException exception
    ) {
        ResponseErrorDto errorDTO = responseUtil.createAPIErrorDTO(exception);
        Response<ResponseErrorDto> response = responseUtil.createAPIResponse(errorDTO);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = ActivityNotFoundException.class)
    public ResponseEntity<Response<ResponseErrorDto>> handleActivityNotFoundException(
            ActivityNotFoundException exception
    ) {
        ResponseErrorDto errorDTO = responseUtil.createAPIErrorDTO(exception);
        Response<ResponseErrorDto> response = responseUtil.createAPIResponse(errorDTO);
        return ResponseEntity.badRequest().body(response);
    }

}
