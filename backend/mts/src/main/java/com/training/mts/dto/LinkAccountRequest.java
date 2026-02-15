package com.training.mts.dto;

public class LinkAccountRequest {
    public String getVpaId() {
        return vpaId;
    }

    public void setVpaId(String vpaId) {
        this.vpaId = vpaId;
    }

    String vpaId;
    String accountNumber;

    String accountPassword;
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }
}
