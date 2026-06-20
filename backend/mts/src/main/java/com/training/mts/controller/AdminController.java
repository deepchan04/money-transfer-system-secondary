package com.training.mts.controller;

import com.training.mts.dto.ChangeStatusRequest;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.dto.UserResponse;
import com.training.mts.service.AdminServiceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {


    private AdminServiceImpl adminService;

    public AdminController(AdminServiceImpl adminService) {

        this.adminService = adminService;
    }


    @GetMapping("/gettranslog")
    public ResponseEntity<List<Transaction>> getTransLog(){
        return new ResponseEntity<>(adminService.getTransactionHistory(), HttpStatus.OK);
    }

    @GetMapping("/getusers")
    public ResponseEntity<List<UserResponse>> getUsers(){
        var list = adminService.findAll().stream().map(UserResponse::from).toList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PutMapping("/changeStatus")
    public ResponseEntity<UserResponse> changeStatus(@RequestBody ChangeStatusRequest changeStatusRequest){
        return new ResponseEntity<>(UserResponse.from(adminService.updateUser(changeStatusRequest.getVpaId(),changeStatusRequest.getAppStatus())),HttpStatus.OK);
    }
}
