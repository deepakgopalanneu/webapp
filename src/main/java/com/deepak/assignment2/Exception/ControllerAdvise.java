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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
