package com.training.mts.service;

import com.training.mts.exceptions.InsufficientUserDataException;
import com.training.mts.exceptions.VPAIdNotFoundException;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.VPARepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VPAServiceImpl implements VPAService{


    private VPARepository vpaRepository;


    public VPAServiceImpl(VPARepository vpaRepository){
        this.vpaRepository = vpaRepository;
    }
    @Override
    public String generateVPAId(User user) throws InsufficientUserDataException {
        String baseHandle;

        if (user.getEmail() != null && user.getPhoneNumber() != null && !user.getEmail().isEmpty()) {
            baseHandle = user.getEmail().split("@")[0] + user.getPhoneNumber().substring(0,4);
        }
        else {
            throw new InsufficientUserDataException("Insufficient user data to generate VPA");
        }

        return baseHandle + user.getId() + "@mts";

    }


    public VPA getVPA(String vpaId) throws VPAIdNotFoundException {
        Optional<VPA> vpa = vpaRepository.findByVpaId(vpaId);
        if(vpa.isPresent()){
            return vpa.get();
        }
        throw new VPAIdNotFoundException("VPA id "+vpaId+" not found");
    }


}

