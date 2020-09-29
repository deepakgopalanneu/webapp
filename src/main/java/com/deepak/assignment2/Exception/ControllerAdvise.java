package com.deepak.assignment2.Exception;

import com.deepak.assignment2.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerAdvise {

    /**
     * this method handles all UserException thrown
     * @param ex
     * @return ResponseEntity of type Error
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<Error> handleUserException(UserException ex) {
        Error error = new Error();
        error.setErrormessage(ex.getMessage());
        if (ex.getMessage().contains("Conflict"))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        else if (ex.getMessage().contains("NotFound"))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * this method handles all UserValidationException thrown
     * @param ex
     * @return ResponseEntity of type Error
     */
    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<Error> handleValidationException(UserValidationException ex) {
        Error error = new Error();
        error.setErrormessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
