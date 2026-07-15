package com.budgetscope.api.identity.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class UserTest {
    @Test
    void createsUserWithoutAvatar() {
        var id = new UserId(UUID.randomUUID());
        var user = User.create(id, new EmailAddress("owner@example.com"), new UserName("Owner"));

        assertEquals(id, user.id());
        assertEquals("owner@example.com", user.email().value());
        assertEquals("Owner", user.name().value());
        assertFalse(user.avatarImage().isPresent());
    }

    @Test
    void createsUserWithAvatarReference() {
        var avatar = new AvatarImage("avatars/user-1.png", "image/png");
        var user = User.create(UserId.newId(), new EmailAddress("owner@example.com"), new UserName("Owner"), avatar);

        assertEquals(avatar, user.avatarImage().orElseThrow());
    }

    @Test
    void changesProfileDetails() {
        var user = User.create(UserId.newId(), new EmailAddress("owner@example.com"), new UserName("Owner"));
        var avatar = new AvatarImage("avatars/user-1.webp", "image/webp");

        user.changeEmail(new EmailAddress("new-owner@example.com"));
        user.changeName(new UserName("New Owner"));
        user.changeAvatar(avatar);

        assertEquals("new-owner@example.com", user.email().value());
        assertEquals("New Owner", user.name().value());
        assertEquals(avatar, user.avatarImage().orElseThrow());
    }

    @Test
    void removesAvatar() {
        var user = User.create(
                UserId.newId(),
                new EmailAddress("owner@example.com"),
                new UserName("Owner"),
                new AvatarImage("avatars/user-1.png", "image/png"));

        user.removeAvatar();

        assertTrue(user.avatarImage().isEmpty());
    }

    @Test
    void requiresProfileDetails() {
        var id = UserId.newId();
        var email = new EmailAddress("owner@example.com");
        var name = new UserName("Owner");

        assertThrows(NullPointerException.class, () -> User.create(null, email, name));
        assertThrows(NullPointerException.class, () -> User.create(id, null, name));
        assertThrows(NullPointerException.class, () -> User.create(id, email, null));
        assertThrows(NullPointerException.class, () -> new UserId(null));
    }
}
