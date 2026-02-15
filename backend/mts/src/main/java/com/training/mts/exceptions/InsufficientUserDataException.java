package com.training.mts.exceptions;

public class InsufficientUserDataException extends RuntimeException{
    public InsufficientUserDataException(String msg){
        super(msg);
    }
}
