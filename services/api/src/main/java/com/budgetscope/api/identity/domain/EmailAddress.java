package com.budgetscope.api.identity.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record EmailAddress(String value) {
    private static final int MAX_LENGTH = 320;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public EmailAddress {
        Objects.requireNonNull(value, "Email address is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Email address is required");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Email address must be at most " + MAX_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email address format is invalid");
        }
    }
}
