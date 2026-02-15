package com.training.mts.dto;


import com.training.mts.enums.AppStatus;

public class ChangeStatusRequest {
    private String vpaId;

    public AppStatus getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(AppStatus appStatus) {
        this.appStatus = appStatus;
    }

    private AppStatus appStatus;

    public String getVpaId() {
        return vpaId;
    }

    public void setVpaId(String vpaId) {
        this.vpaId = vpaId;
    }


}
