package com.ugr.microservices.dependencies.core.tddt4iots.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<GenericBasicResponse<Void>> handleGenericException(GenericException ex) {
        log.error("Handling GenericException - Message: {}, Code: {}, Status: {}",
                ex.getUserMessageError(), ex.getCodeError(), ex.getStatusError());

        GenericBasicResponse<Void> response = new GenericBasicResponse<>();
        response.setCode(ex.getCodeError());
        response.setStatus(ex.getStatusError());
        response.setMessage(ex.getUserMessageError());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericBasicResponse<Void>> handleException(Exception ex) {
        log.error("Handling Exception - Message: {}", ex.getMessage(), ex);

        GenericBasicResponse<Void> response = new GenericBasicResponse<>();
        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setStatus("ERROR");
        response.setMessage("An unexpected error occurred.");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
