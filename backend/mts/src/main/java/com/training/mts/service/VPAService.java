package com.training.mts.service;

import com.training.mts.exceptions.InsufficientUserDataException;
import com.training.mts.model.User;

public interface VPAService {
    public String generateVPAId(User user) throws InsufficientUserDataException;
}
