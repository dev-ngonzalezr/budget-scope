package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.User;
import com.budgetscope.api.workspace.domain.Workspace;
import java.util.Objects;

public record RegisteredUserResult(User user, Workspace defaultWorkspace) {
    public RegisteredUserResult {
        Objects.requireNonNull(user, "Registered user is required");
        Objects.requireNonNull(defaultWorkspace, "Default workspace is required");
    }
}
