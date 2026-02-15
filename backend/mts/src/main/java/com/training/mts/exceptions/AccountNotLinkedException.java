package com.training.mts.exceptions;

public class AccountNotLinkedException extends RuntimeException{

    public AccountNotLinkedException(String msg){
        super(msg);
    }
}
