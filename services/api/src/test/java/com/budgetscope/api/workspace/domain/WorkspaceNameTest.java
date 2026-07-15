package com.budgetscope.api.workspace.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class WorkspaceNameTest {
    @Test
    void trimsWorkspaceName() {
        var name = new WorkspaceName("  Family Budget  ");

        assertEquals("Family Budget", name.value());
    }

    @Test
    void rejectsBlankWorkspaceName() {
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceName(""));
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceName("   "));
    }
}
