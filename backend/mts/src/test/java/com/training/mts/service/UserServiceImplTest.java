package com.training.mts.service;

import com.training.mts.dto.GetAllUsersRequest;
import com.training.mts.exceptions.AccountNotLinkedException;
import com.training.mts.exceptions.IncorrectPasswordException;
import com.training.mts.exceptions.UserNotFoundException;
import com.training.mts.exceptions.VPAIdNotFoundException;
import com.training.mts.model.BankAccount;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.UserRepository;
import com.training.mts.repository.VPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock private VPAServiceImpl vpaService;
    @Mock private VPARepository vpaRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private VPA vpa;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Diya");
        user.setPhoneNumber("9999999999");
        user.setPassword("encodedPass");

        vpa = new VPA();
        vpa.setVpaId("diya@upi");
        vpa.setUser(user);

        user.setVpa(vpa);
    }
    @Test
    void createUser_success() {

        when(passwordEncoder.encode("rawPass"))
                .thenReturn("encodedPass");

        when(vpaService.generateVPAId(any(User.class)))
                .thenReturn("diya@upi");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(vpaRepository.save(any(VPA.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(
                "Diya", "9999999999", "diya@gmail.com", "rawPass");

        assertNotNull(created);
        assertEquals("Diya", created.getName());
        assertEquals("encodedPass", created.getPassword());
    }
    @Test
    void getUserByPhone_success() {

        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByPhoneNumber("9999999999");

        assertEquals(user, result);
    }
    @Test
    void getUserByPhone_notFound() {

        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.getUserByPhoneNumber("9999999999"));
    }
    @Test
    void getBankBalance_success() throws Exception {

        BankAccount account = new BankAccount();
        account.setBalance(7000.0);

        user.setBankAccount(account);

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        when(passwordEncoder.matches("rawPass", user.getPassword()))
                .thenReturn(true);

        Double balance = userService.getBankBalance("diya@upi", "rawPass");

        assertEquals(7000.0, balance);
    }
    @Test
    void getBankBalance_invalidPassword() {

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        assertThrows(IncorrectPasswordException.class, () ->
                userService.getBankBalance("diya@upi", "wrong"));
    }
    @Test
    void getBankBalance_accountNotLinked() {

        user.setBankAccount(null);

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        assertThrows(AccountNotLinkedException.class, () ->
                userService.getBankBalance("diya@upi", "rawPass"));
    }
    @Test
    void getAllUsers_filtersAdmin() {

        User admin = new User();
        admin.setRole(com.training.mts.enums.Role.ROLE_ADMIN);

        user.setRole(com.training.mts.enums.Role.ROLE_USER);

        when(userRepository.findAll())
                .thenReturn(List.of(user, admin));

        List<GetAllUsersRequest> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("Diya", result.get(0).getName());
    }
    @Test
    void getUserByVpaId_success() {

        when(vpaRepository.findByVpaId("diya@upi"))
                .thenReturn(Optional.of(vpa));

        User result = userService.getUserByVpaId("diya@upi");

        assertEquals(user, result);
    }
    @Test
    void getUserByVpaId_notFound() {

        when(vpaRepository.findByVpaId("invalid@upi"))
                .thenReturn(Optional.empty());

        assertThrows(VPAIdNotFoundException.class, () ->
                userService.getUserByVpaId("invalid@upi"));
    }
    @Test
    void getVpaId_success() {

        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.of(user));

        String result = userService.getVpaId("9999999999");

        assertEquals("diya@upi", result);
    }
    @Test
    void getVpaId_userNotFound() {

        when(userRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.getVpaId("9999999999"));
    }
    @Test
    void getBankBalance_userNotFound() {

        when(vpaRepository.findByVpaId("invalid@upi"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.getBankBalance("invalid@upi", "pass"));
    }

}
