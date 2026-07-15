package com.budgetscope.api.workspace.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.budgetscope.api.identity.domain.UserId;
import org.junit.jupiter.api.Test;

final class WorkspaceTest {
    @Test
    void createsWorkspaceWithOwner() {
        var ownerId = UserId.newId();
        var workspace = Workspace.create(
                WorkspaceId.newId(),
                new WorkspaceName("Family Budget"),
                ownerId);

        assertEquals("Family Budget", workspace.name().value());
        assertEquals(ownerId, workspace.ownerId());
        assertTrue(workspace.isOwnedBy(ownerId));
    }

    @Test
    void renamesWorkspace() {
        var workspace = Workspace.create(
                WorkspaceId.newId(),
                new WorkspaceName("Family Budget"),
                UserId.newId());

        workspace.rename(new WorkspaceName("Household Budget"));

        assertEquals("Household Budget", workspace.name().value());
    }

    @Test
    void identifiesNonOwner() {
        var workspace = Workspace.create(
                WorkspaceId.newId(),
                new WorkspaceName("Family Budget"),
                UserId.newId());

        assertFalse(workspace.isOwnedBy(UserId.newId()));
    }

    @Test
    void requiresWorkspaceDetails() {
        var id = WorkspaceId.newId();
        var name = new WorkspaceName("Family Budget");
        var ownerId = UserId.newId();

        assertThrows(NullPointerException.class, () -> Workspace.create(null, name, ownerId));
        assertThrows(NullPointerException.class, () -> Workspace.create(id, null, ownerId));
        assertThrows(NullPointerException.class, () -> Workspace.create(id, name, null));
        assertThrows(NullPointerException.class, () -> new WorkspaceId(null));
        var workspace = Workspace.create(id, name, ownerId);
        assertThrows(NullPointerException.class, () -> workspace.rename(null));
        assertThrows(NullPointerException.class, () -> workspace.isOwnedBy(null));
    }
}
