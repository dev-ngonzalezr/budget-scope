package com.budgetscope.api.identity.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class AvatarImageTest {
    @Test
    void normalizesAvatarReference() {
        var avatar = new AvatarImage("  avatars/user-1.PNG  ", "  IMAGE/PNG  ");

        assertEquals("avatars/user-1.PNG", avatar.storageKey());
        assertEquals("image/png", avatar.contentType());
    }

    @Test
    void rejectsInvalidAvatarReference() {
        assertThrows(IllegalArgumentException.class, () -> new AvatarImage("", "image/png"));
        assertThrows(IllegalArgumentException.class, () -> new AvatarImage("avatars/user-1.txt", "text/plain"));
        assertThrows(IllegalArgumentException.class, () -> new AvatarImage("avatars/user-1.png", ""));
    }
}
