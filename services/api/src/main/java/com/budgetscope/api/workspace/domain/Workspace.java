package com.budgetscope.api.workspace.domain;

import com.budgetscope.api.identity.domain.UserId;
import java.util.Objects;

public final class Workspace {
    private final WorkspaceId id;
    private final UserId ownerId;
    private WorkspaceName name;

    public Workspace(WorkspaceId id, WorkspaceName name, UserId ownerId) {
        this.id = Objects.requireNonNull(id, "Workspace id is required");
        this.name = Objects.requireNonNull(name, "Workspace name is required");
        this.ownerId = Objects.requireNonNull(ownerId, "Workspace owner id is required");
    }

    public static Workspace create(WorkspaceId id, WorkspaceName name, UserId ownerId) {
        return new Workspace(id, name, ownerId);
    }

    public WorkspaceId id() {
        return id;
    }

    public WorkspaceName name() {
        return name;
    }

    public UserId ownerId() {
        return ownerId;
    }

    public boolean isOwnedBy(UserId userId) {
        return ownerId.equals(Objects.requireNonNull(userId, "Workspace owner id is required"));
    }

    public void rename(WorkspaceName newName) {
        name = Objects.requireNonNull(newName, "Workspace name is required");
    }
}
