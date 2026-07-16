package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.User;
import java.util.Objects;

public record RegisteredUserResult(User user) {
    public RegisteredUserResult {
        Objects.requireNonNull(user, "Registered user is required");
    }
}
