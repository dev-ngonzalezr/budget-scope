package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.shared.application.InternalEvent;
import java.util.Objects;

public record UserRegisteredEvent(UserId userId, EmailAddress email) implements InternalEvent {
    public UserRegisteredEvent {
        Objects.requireNonNull(userId, "Registered user id is required");
        Objects.requireNonNull(email, "Registered user email is required");
    }
}
