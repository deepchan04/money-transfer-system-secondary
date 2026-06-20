package com.training.mts.dto;

public class BankAccountResponse {
    private String accountNumber;

    public BankAccountResponse() {}

    public BankAccountResponse(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
