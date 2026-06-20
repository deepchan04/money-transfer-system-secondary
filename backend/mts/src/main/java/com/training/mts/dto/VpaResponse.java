package com.training.mts.dto;

public class VpaResponse {
    private String vpaId;

    public VpaResponse() {}

    public VpaResponse(String vpaId) {
        this.vpaId = vpaId;
    }

    public String getVpaId() {
        return vpaId;
    }

    public void setVpaId(String vpaId) {
        this.vpaId = vpaId;
    }
}
