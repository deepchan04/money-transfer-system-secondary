package com.training.mts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotLinkedException.class)
    public ResponseEntity<String> handleAccountNotLinkedFoundException(AccountNotLinkedException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.PRECONDITION_FAILED);

    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<String> handleAccountNotActiveException(AccountNotActiveException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);

    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalanceException(InsufficientBalanceException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(DuplicateTransferException.class)
    public ResponseEntity<String> handleDuplicateTransferException(DuplicateTransferException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<String> handleDuplicateUserException(DuplicateUserException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<String> handleIncorrectPasswordException(IncorrectPasswordException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(AccountLinkedException.class)
    public ResponseEntity<String> handleAccountLinkedException(AccountLinkedException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

    }

    @ExceptionHandler(VPAIdNotFoundException.class)
    public ResponseEntity<String> handleVPAIdNotFoundException(VPAIdNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<String> handleInvalidAmountException(InvalidAmountException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<String> handleAuthenticationException(Exception e) {
        // We use 401 Unauthorized for both to hide whether the user exists or just the password was wrong
        return new ResponseEntity<>("Invalid phone number or password", HttpStatus.UNAUTHORIZED);
    }


}
