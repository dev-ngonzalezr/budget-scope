package com.budgetscope.api.workspace.application;

import com.budgetscope.api.identity.application.UserRegisteredEvent;
import com.budgetscope.api.shared.application.InternalEventHandler;
import com.budgetscope.api.workspace.domain.Workspace;
import com.budgetscope.api.workspace.domain.WorkspaceId;
import com.budgetscope.api.workspace.domain.WorkspaceName;
import java.util.Objects;

public final class CreateDefaultWorkspaceOnUserRegistered implements InternalEventHandler<UserRegisteredEvent> {
    private static final WorkspaceName DEFAULT_WORKSPACE_NAME = new WorkspaceName("default");

    private final WorkspaceRepository workspaceRepository;

    public CreateDefaultWorkspaceOnUserRegistered(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = Objects.requireNonNull(workspaceRepository, "Workspace repository is required");
    }

    @Override
    public Class<UserRegisteredEvent> eventType() {
        return UserRegisteredEvent.class;
    }

    @Override
    public void handle(UserRegisteredEvent event) {
        Objects.requireNonNull(event, "User registered event is required");

        var workspace = Workspace.create(WorkspaceId.newId(), DEFAULT_WORKSPACE_NAME, event.userId());
        workspaceRepository.save(workspace);
    }
}
