package com.training.mts.exceptions;

public class DuplicateTransferException extends RuntimeException{
    public DuplicateTransferException(String msg){
        super(msg);
    }
}
