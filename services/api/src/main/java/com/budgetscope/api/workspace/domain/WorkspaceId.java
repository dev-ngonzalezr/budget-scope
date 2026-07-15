package com.budgetscope.api.workspace.domain;

import java.util.Objects;
import java.util.UUID;

public record WorkspaceId(UUID value) {
    public WorkspaceId {
        Objects.requireNonNull(value, "Workspace id is required");
    }

    public static WorkspaceId newId() {
        return new WorkspaceId(UUID.randomUUID());
    }
}
