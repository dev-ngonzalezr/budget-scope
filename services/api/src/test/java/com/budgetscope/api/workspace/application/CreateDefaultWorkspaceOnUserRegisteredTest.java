package com.budgetscope.api.workspace.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.budgetscope.api.identity.application.UserRegisteredEvent;
import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.workspace.domain.Workspace;
import org.junit.jupiter.api.Test;

final class CreateDefaultWorkspaceOnUserRegisteredTest {
    @Test
    void createsDefaultWorkspaceForRegisteredUserEvent() {
        var repository = new RecordingWorkspaceRepository();
        var handler = new CreateDefaultWorkspaceOnUserRegistered(repository);
        var userId = UserId.newId();

        var workspace = handler.handle(new UserRegisteredEvent(userId, new EmailAddress("owner@example.com")));

        assertSame(workspace, repository.savedWorkspace);
        assertEquals(userId, workspace.ownerId());
        assertEquals("default", workspace.name().value());
    }

    @Test
    void requiresRepositoryAndEvent() {
        var handler = new CreateDefaultWorkspaceOnUserRegistered(new RecordingWorkspaceRepository());

        assertThrows(NullPointerException.class, () -> new CreateDefaultWorkspaceOnUserRegistered(null));
        assertThrows(NullPointerException.class, () -> handler.handle(null));
    }

    private static final class RecordingWorkspaceRepository implements WorkspaceRepository {
        private Workspace savedWorkspace;

        @Override
        public Workspace save(Workspace workspace) {
            savedWorkspace = workspace;
            return workspace;
        }
    }
}
