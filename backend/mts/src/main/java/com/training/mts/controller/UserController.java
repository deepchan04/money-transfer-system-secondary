package com.training.mts.controller;

import com.training.mts.dto.GetAllUsersRequest;
import com.training.mts.dto.UserResponse;
import com.training.mts.dto.GetBalanceRequest;
import com.training.mts.exceptions.AccountNotLinkedException;
import com.training.mts.exceptions.IncorrectPasswordException;
import com.training.mts.model.User;
import com.training.mts.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {


    private UserServiceImpl userService;

    public UserController(UserServiceImpl userService){
        this.userService = userService;
    }


    @PostMapping("/getbalance")
    public ResponseEntity<Double> getBalance(@RequestBody GetBalanceRequest request) throws AccountNotLinkedException, IncorrectPasswordException {
        return new ResponseEntity<>(userService.getBankBalance(request.getVpaId(), request.getPassword()), HttpStatus.OK);
    }


    @GetMapping("/findByPhone")
    public ResponseEntity<UserResponse> getUser(@RequestParam String phoneNumber) {
        return new ResponseEntity<>(UserResponse.from(userService.getUserByPhoneNumber(phoneNumber)),HttpStatus.OK);
    }
    @GetMapping("/getusers")
    public ResponseEntity<List<GetAllUsersRequest>> getUsers() {
        return new ResponseEntity<>(userService.getAllUsers(),HttpStatus.OK);
    }
    @GetMapping("/getvpa")
    public ResponseEntity<String> getVpa(@RequestParam String phone) {
        return new ResponseEntity<>(userService.getVpaId(phone),HttpStatus.OK);
    }
    @GetMapping("/search")
    public ResponseEntity<List<GetAllUsersRequest>> searchUsers(
            @RequestParam String query) {

        return ResponseEntity.ok(
                userService.searchUsers(query)
        );
    }


}

