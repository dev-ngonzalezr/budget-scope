package com.budgetscope.api.workspace.domain;

import java.util.Objects;

public record WorkspaceName(String value) {
    private static final int MAX_LENGTH = 160;

    public WorkspaceName {
        Objects.requireNonNull(value, "Workspace name is required");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Workspace name is required");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Workspace name must be at most " + MAX_LENGTH + " characters");
        }
    }
}
