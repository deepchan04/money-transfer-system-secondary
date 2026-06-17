package com.training.mts.model;

import com.training.mts.enums.AppStatus;
import jakarta.persistence.*;
import com.training.mts.enums.Role;

@Entity
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinTable(name = "user_bank_accounts", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "account_id"))
    private BankAccount bankAccount;
    private String name;
    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    public AppStatus getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(AppStatus appStatus) {
        this.appStatus = appStatus;
    }

    private AppStatus appStatus;

    @OneToOne(mappedBy = "user")
    private VPA vpa;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public VPA getVpa() {
        return vpa;
    }

    public void setVpa(VPA vpa) {
        this.vpa = vpa;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(columnDefinition = "int default 0")
    private Integer rewardPoints = 0;

    @Column(columnDefinition = "int default 0")
    private Integer totalPointsEarned = 0;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getRewardPoints() {
        return rewardPoints != null ? rewardPoints : 0;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public Integer getTotalPointsEarned() {
        return totalPointsEarned != null ? totalPointsEarned : 0;
    }

    public void setTotalPointsEarned(Integer totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", vpa=" + vpa +
                '}';
    }

    // Getters and Setters
}
