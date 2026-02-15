package com.training.mts.service;

import com.training.mts.enums.AppStatus;

import com.training.mts.exceptions.VPAIdNotFoundException;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.TransactionRepository;
import com.training.mts.repository.UserRepository;
import com.training.mts.repository.VPARepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService{

    private VPARepository vpaRepository;
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    public AdminServiceImpl(UserRepository userRepository,VPARepository vpaRepository, TransactionRepository transactionRepository){
        this.vpaRepository = vpaRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Transaction> getTransactionHistory() {

        return transactionRepository.findAll();
    }
    @Override
    public List<User> findAll(){
        return userRepository.findAll();
    }

    @Override
    public User updateUser(String vpaId, AppStatus appStatus){
        Optional<VPA> vpa = vpaRepository.findByVpaId(vpaId);
        if(!vpa.isPresent()){
            throw new VPAIdNotFoundException("VPA id "+vpaId+" not found");
        }
        User user = vpa.get().getUser();
        user.setAppStatus(appStatus);
        return userRepository.save(user);
    }
}
