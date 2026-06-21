package com.training.mts.service;

import com.training.mts.dto.GetAllUsersRequest;
import com.training.mts.exceptions.*;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.UserRepository;
import com.training.mts.exceptions.DuplicateUserException;
import com.training.mts.repository.VPARepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {


    private UserRepository userRepository;
    private VPAServiceImpl vpaService;
    private VPARepository vpaRepository;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,VPAServiceImpl vpaService,VPARepository vpaRepository,PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.vpaService = vpaService;
        this.vpaRepository = vpaRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public User createUser(String name, String phoneNumber, String email, String password)
            throws InsufficientUserDataException {
        User user = new User();
        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(com.training.mts.enums.Role.ROLE_USER);
        user.setAppStatus(com.training.mts.enums.AppStatus.ACTIVE);
        // Check for duplicates before saving to provide a clear error
        List<String> conflicts = new ArrayList<>();
        if (phoneNumber != null && userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            conflicts.add("Phone number");
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            conflicts.add("Email");
        }

        if (!conflicts.isEmpty()) {
            String message;
            if (conflicts.size() == 1) {
                message = conflicts.get(0) + " already registered";
            } else {
                message = String.join(" and ", conflicts) + " already registered";
            }
            throw new DuplicateUserException(message);
        }

        userRepository.save(user);
        String vpaId = vpaService.generateVPAId(user);
        VPA vpa = new VPA();
        vpa.setVpaId(vpaId);
        vpa.setUser(user);
        vpaRepository.save(vpa);
        user.setVpa(vpa);
        return user;
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if(user.isPresent()){
            return user.get();
        }

        throw new UserNotFoundException("User with phone number " + phoneNumber + " not found");
    }

    public User getUserByVpaId(String vpaId) {
        Optional<VPA> vpa = vpaRepository.findByVpaId(vpaId);

        if (vpa.isPresent()) {
            return vpa.get().getUser();
        }
        throw new VPAIdNotFoundException("VPA id " + vpaId + " not found");
    }
    public String getVpaId(String phoneNumber) {
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if(user.isPresent()){
            return user.get().getVpa().getVpaId();
        }
        throw new UserNotFoundException("User not found");



    }

    public Double getBankBalance(String vpaId, String password)
            throws AccountNotLinkedException, IncorrectPasswordException {

        User user = vpaRepository.findByVpaId(vpaId)
                .map(VPA::getUser)
                .orElseThrow(() -> new UserNotFoundException("User with VPA " + vpaId + " not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException("Password Incorrect!!");
        }

        if (user.getBankAccount() == null) {
            throw new AccountNotLinkedException("Account not linked");
        }

        return user.getBankAccount().getBalance();
    }


    public List<GetAllUsersRequest> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<GetAllUsersRequest> requests = new ArrayList<>(); // Initialize the list

        for (User user : users) {
            if(!user.getRole().name().equals("ROLE_ADMIN")){
            GetAllUsersRequest request = new GetAllUsersRequest();
            request.setName(user.getName());
            request.setVpaId(user.getVpa().getVpaId());

            requests.add(request);
            }
            // Add to the result list
        }

        return requests;
    }

    public List<GetAllUsersRequest> searchUsers(String query) {

        List<User> users = userRepository.searchUsers(query);

        List<GetAllUsersRequest> results = new ArrayList<>();

        for (User user : users) {

            if(user.getRole().name().equals("ROLE_ADMIN")) {
                continue;
            }

            GetAllUsersRequest dto = new GetAllUsersRequest();

            dto.setName(user.getName());
            dto.setVpaId(user.getVpa().getVpaId());

            results.add(dto);
        }

        return results;
    }

}
