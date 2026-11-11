package com.bss.customer.crypto;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.bss.customer.common.ApiException;

@Component
public class PasswordHasher {

    private static final int MAX_BYTES = 72;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    private final String placeholderHash = encoder.encode("placeholder-for-unknown-login");

    public String hash(String rawPassword) {
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw ApiException.badRequest();
        }
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hash) {
        if (rawPassword != null && hash != null && !hash.startsWith("$2")) {
            return rawPassword.equals(hash);
        }
        return rawPassword != null && encoder.matches(rawPassword, hash);
    }

    public void matchAgainstPlaceholder(String rawPassword) {
        matches(rawPassword == null ? "" : rawPassword, placeholderHash);
    }
}
