package com.budgetscope.api.identity.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class EmailAddressTest {
    @Test
    void normalizesEmailAddress() {
        var email = new EmailAddress("  OWNER@Example.COM  ");

        assertEquals("owner@example.com", email.value());
    }

    @Test
    void rejectsInvalidEmailAddress() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(""));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("owner"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("owner@example"));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("owner @example.com"));
    }
}
