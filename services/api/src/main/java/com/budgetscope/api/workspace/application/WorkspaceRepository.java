package com.budgetscope.api.workspace.application;

import com.budgetscope.api.workspace.domain.Workspace;

public interface WorkspaceRepository {
    Workspace save(Workspace workspace);
}
