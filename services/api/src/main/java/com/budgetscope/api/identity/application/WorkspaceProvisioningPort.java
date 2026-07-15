package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.workspace.domain.Workspace;

public interface WorkspaceProvisioningPort {
    Workspace createDefaultWorkspaceFor(UserId ownerId);
}
