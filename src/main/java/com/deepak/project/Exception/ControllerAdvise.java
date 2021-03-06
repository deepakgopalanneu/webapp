package com.deepak.project.Exception;

import com.deepak.project.model.Error;
import com.deepak.project.util.CustomStrings;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerAdvise extends ResponseEntityExceptionHandler {


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
        if (error.getErrormessage().equals(CustomStrings.user_not_found))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        if (error.getErrormessage().equals(CustomStrings.user_conflict))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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
        if (error.getErrormessage().equals(CustomStrings.not_found) || error.getErrormessage().equals(CustomStrings.answer_notfound))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        if (error.getErrormessage().equals(CustomStrings.forbidden))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * this method handles all FileException thrown
     *
     * @param ex
     * @return ResponseEntity of type Error
     */
    @ExceptionHandler(FileException.class)
    public ResponseEntity<Error> handleFileException(FileException ex) {
        Error error = new Error();
        error.setErrormessage(ex.getMessage());
        if (null != ex.getDescription())
            error.setDescription(ex.getDescription());
        if (error.getErrormessage().equals(CustomStrings.typeUnsupported))
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
        if (error.getErrormessage().equals(CustomStrings.file_notfound))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        if(error.getErrormessage().equals(CustomStrings.file_exists))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * this method handles all the other exceptions thrown
     *
     * @param ex
     * @return ResponseEntity of type Object
     */
    public ResponseEntity<Object> commonhandler(Exception ex, HttpStatus status) {
        Error error = new Error();
        error.setErrormessage(ex.getMessage());
        error.setDescription(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUnexpectedExceptions(Exception ex) {
        Error error = new Error();
        error.setErrormessage(ex.getMessage());
        error.setDescription(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(error);
    }
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        return commonhandler(ex, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return commonhandler(ex, status);
    }
}
