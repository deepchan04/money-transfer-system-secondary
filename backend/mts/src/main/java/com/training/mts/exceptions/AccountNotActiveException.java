package com.training.mts.exceptions;

public class AccountNotActiveException extends RuntimeException{
    public AccountNotActiveException(String msg){
        super(msg);
    }
}
