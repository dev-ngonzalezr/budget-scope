package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.EmailAddress;
import java.util.Objects;

public final class DuplicateUserEmailException extends RuntimeException {
    private final EmailAddress email;

    public DuplicateUserEmailException(EmailAddress email) {
        super("A user already exists for email " + Objects.requireNonNull(email, "Email address is required").value());
        this.email = email;
    }

    public EmailAddress email() {
        return email;
    }
}
