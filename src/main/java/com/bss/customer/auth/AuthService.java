package com.bss.customer.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.crypto.PasswordHasher;

@Service
public class AuthService {

    private final AccountMapper accountMapper;
    private final PasswordHasher passwordHasher;
    private final int lockThreshold;

    public AuthService(AccountMapper accountMapper, PasswordHasher passwordHasher,
                       @Value("${app.login.lock-threshold}") int lockThreshold) {
        this.accountMapper = accountMapper;
        this.passwordHasher = passwordHasher;
        this.lockThreshold = lockThreshold;
    }

    public Account login(String loginId, String password) {
        Account account = loginId == null ? null : accountMapper.findByLoginId(loginId);
        if (account == null) {
            passwordHasher.matchAgainstPlaceholder(password);
            throw handleLoginFailure(null);
        }
        if (!passwordHasher.matches(password, account.getPasswordHash())) {
            throw handleLoginFailure(account);
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new LoginFailedException();
        }
        accountMapper.recordLoginSuccess(account.getAccountId(), Instant.now());
        return account;
    }

    public void logout(String accountId) {
        accountMapper.endSessions(accountId);
    }

    private LoginFailedException handleLoginFailure(Account account) {
        if (account == null) {
            return new LoginFailedException("존재하지 않는 아이디입니다.");
        }
        return new LoginFailedException("비밀번호가 일치하지 않습니다.");
    }
}
