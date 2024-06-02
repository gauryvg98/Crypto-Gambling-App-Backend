package com.cryptoclyx.server.config.exception;

import com.cryptoclyx.server.exceptions.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * Spring web exception handling customization.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.BAD_REQUEST.value());
        String errorMsg = ex.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        errorObject.setMessage(errorMsg);
        errorObject.setDetails(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleNicknameException(NicknameException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.CONFLICT.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleEmailException(EmailException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.CONFLICT.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.BAD_REQUEST.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handlePasswordDoesNotMatchException(PasswordDoesNotMatchException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.BAD_REQUEST.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleVerificationTokenNotFoundException(VerificationTokenNotFoundException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleVerificationTokenExpiredException(VerificationTokenExpiredException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.FORBIDDEN.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleBadCredentialsException(BadCredentialsException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.FORBIDDEN.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleWalletException(WalletException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleTransactionLogNotFoundException(TransactionLogNotFoundException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleTransactionLogEntityExpired(TransactionLogEntityExpired ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.GONE.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.GONE);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleTransactionLogEntityAlreadyCommitted(TransactionLogEntityAlreadyCommitted ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.OK.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleTIllegalArgumentException(IllegalArgumentException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.BAD_REQUEST.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handleTemporaryWithdrawalUnavailableException(TemporaryWithdrawalUnavailableException ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        errorObject.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseObject> handlePhoneNumberConfirmationRequired(PhoneNumberConfirmationRequired ex) {
        ResponseObject errorObject = new ResponseObject();
        errorObject.setHttpStatus(HttpStatus.UPGRADE_REQUIRED.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setDetails(ex.getToken());
        return new ResponseEntity<>(errorObject, HttpStatus.UPGRADE_REQUIRED);
    }
}