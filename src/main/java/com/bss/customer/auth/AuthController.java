package com.bss.customer.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.account.Account;
import com.bss.customer.security.LoginAccount;
import com.bss.customer.security.SessionKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final com.bss.customer.customer.CustomerMapper customerMapper;

    public AuthController(AuthService authService, com.bss.customer.customer.CustomerMapper customerMapper) {
        this.authService = authService;
        this.customerMapper = customerMapper;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Account account = authService.login(request.loginId(), request.password());

        HttpSession previous = httpRequest.getSession(false);
        if (previous != null) {
            previous.invalidate();
        }
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SessionKeys.ACCOUNT_ID, account.getAccountId());
        session.setAttribute(SessionKeys.SESSION_EPOCH, account.getSessionEpoch());
        session.setAttribute(SessionKeys.ROLE, account.getRole());
        session.setAttribute(SessionKeys.ASSIGNED_CUSTOMERS, customerMapper.findAssignedIds(account.getAccountId()));
        return new LoginResponse(account.getAccountId(), account.getRole());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal LoginAccount actor, HttpServletRequest httpRequest) {
        authService.logout(actor.accountId());
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
