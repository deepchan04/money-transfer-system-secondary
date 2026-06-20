package com.training.mts.dto;

public class GetBalanceRequest {
    private String vpaId;
    private String password;

    public GetBalanceRequest() {
    }

    public GetBalanceRequest(String vpaId, String password) {
        this.vpaId = vpaId;
        this.password = password;
    }

    public String getVpaId() {
        return vpaId;
    }

    public void setVpaId(String vpaId) {
        this.vpaId = vpaId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
