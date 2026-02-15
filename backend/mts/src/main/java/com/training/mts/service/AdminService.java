package com.training.mts.service;


import com.training.mts.enums.AppStatus;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;

import java.util.List;

public interface AdminService {
    public List<Transaction> getTransactionHistory();
    public List<User> findAll();

    public User updateUser(String vpaId, AppStatus appStatus);
}
