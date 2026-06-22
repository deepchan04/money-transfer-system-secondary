package com.training.mts.dto;

import com.training.mts.model.User;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private Integer rewardPoints;
    private Integer totalPointsEarned;
    private String appStatus;
    private VpaResponse vpa;
    private Boolean bankAccountLinked;
    
    private BankAccountResponse bankAccount;



    public static UserResponse from(User user) {
        if (user == null) return null;
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.name = user.getName();
        r.email = user.getEmail();
        r.phoneNumber = user.getPhoneNumber();
        r.role = user.getRole() != null ? user.getRole().name() : null;
        r.rewardPoints = user.getRewardPoints();
        r.totalPointsEarned = user.getTotalPointsEarned();
        r.appStatus = user.getAppStatus() != null ? user.getAppStatus().name() : null;
        if (user.getVpa() != null) {
            r.vpa = new VpaResponse(user.getVpa().getVpaId());
        }
        if (user.getBankAccount() != null) {
            r.bankAccountLinked = true;
            String acct = user.getBankAccount().getAccountNumber();
            String masked = acct;
            if (acct != null && acct.length() >= 4) {
                masked = "****" + acct.substring(acct.length() - 4);
            }
            r.bankAccount = new BankAccountResponse(masked);
        } else {
            r.bankAccountLinked = false;
        }
        return r;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }
    public Integer getTotalPointsEarned() { return totalPointsEarned; }
    public void setTotalPointsEarned(Integer totalPointsEarned) { this.totalPointsEarned = totalPointsEarned; }
    public String getAppStatus() { return appStatus; }
    public void setAppStatus(String appStatus) { this.appStatus = appStatus; }
    public VpaResponse getVpa() { return vpa; }
    public void setVpa(VpaResponse vpa) { this.vpa = vpa; }
    public Boolean getBankAccountLinked() { return bankAccountLinked; }
    public void setBankAccountLinked(Boolean bankAccountLinked) { this.bankAccountLinked = bankAccountLinked; }
    
    public BankAccountResponse getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccountResponse bankAccount) { this.bankAccount = bankAccount; }
}
