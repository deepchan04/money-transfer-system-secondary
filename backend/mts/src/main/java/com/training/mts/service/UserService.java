package com.training.mts.service;

import com.training.mts.dto.GetAllUsersRequest;
import com.training.mts.exceptions.AccountNotLinkedException;
import com.training.mts.exceptions.IncorrectPasswordException;
import com.training.mts.exceptions.InsufficientUserDataException;
import com.training.mts.model.User;

import java.util.List;

public interface UserService {
    public User createUser(String name, String phoneNumber, String email, String password) throws InsufficientUserDataException;
    public User getUserByPhoneNumber(String phoneNumber);
    public User getUserByVpaId(String vpaId);
    public List<GetAllUsersRequest> getAllUsers();
    public Double getBankBalance(String vpaId, String password) throws AccountNotLinkedException, IncorrectPasswordException;
    public List<GetAllUsersRequest> searchUsers(String query);
}
