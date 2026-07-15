package com.budgetscope.api.identity.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class UserNameTest {
    @Test
    void trimsUserName() {
        var name = new UserName("  Budget Owner  ");

        assertEquals("Budget Owner", name.value());
    }

    @Test
    void rejectsBlankUserName() {
        assertThrows(IllegalArgumentException.class, () -> new UserName(""));
        assertThrows(IllegalArgumentException.class, () -> new UserName("   "));
    }
}
