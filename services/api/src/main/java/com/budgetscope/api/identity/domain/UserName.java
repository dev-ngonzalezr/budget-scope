package com.budgetscope.api.identity.domain;

import java.util.Objects;

public record UserName(String value) {
    private static final int MAX_LENGTH = 160;

    public UserName {
        Objects.requireNonNull(value, "User name is required");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("User name is required");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("User name must be at most " + MAX_LENGTH + " characters");
        }
    }
}
