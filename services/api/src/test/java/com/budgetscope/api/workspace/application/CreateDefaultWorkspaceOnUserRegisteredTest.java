package com.budgetscope.api.workspace.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.budgetscope.api.identity.application.UserRegisteredEvent;
import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.shared.infrastructure.InMemoryAsynchronousInternalEventBus;
import com.budgetscope.api.workspace.domain.Workspace;
import org.junit.jupiter.api.Test;

final class CreateDefaultWorkspaceOnUserRegisteredTest {
    @Test
    void subscribesToRegisteredUserEventAndCreatesDefaultWorkspace() {
        var repository = new RecordingWorkspaceRepository();
        var handler = new CreateDefaultWorkspaceOnUserRegistered(repository);
        var events = new InMemoryAsynchronousInternalEventBus(Runnable::run);
        var userId = UserId.newId();

        events.subscribe(handler);
        events.publish(new UserRegisteredEvent(userId, new EmailAddress("owner@example.com")));

        assertEquals(UserRegisteredEvent.class, handler.eventType());
        assertEquals(userId, repository.savedWorkspace.ownerId());
        assertEquals("default", repository.savedWorkspace.name().value());
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
