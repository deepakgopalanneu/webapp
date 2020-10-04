package com.deepak.assignment2.Exception;

import com.deepak.assignment2.model.Error;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerAdvise  extends ResponseEntityExceptionHandler{

    /**
     * this method handles all UserException thrown
     *
     * @param ex
     * @return ResponseEntity of type Error
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<Error> handleUserException(UserException ex) {
        Error error = new Error();
        error.setErrormessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * this method handles all QuestionException thrown
     *
     * @param ex
     * @return ResponseEntity of type Error
     */
    @ExceptionHandler(QuestionException.class)
    public ResponseEntity<Error> handleUserException(QuestionException ex) {
        Error error = new Error();
        error.setErrormessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
