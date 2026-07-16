package com.budgetscope.api.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.budgetscope.api.identity.domain.AvatarImage;
import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.User;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.identity.domain.UserName;
import com.budgetscope.api.shared.application.InternalEvent;
import com.budgetscope.api.shared.application.AsynchronousInternalEventBus;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class RegisterUserUseCaseTest {
    @Test
    void registersUserAndPublishesRegistrationEvent() {
        var users = new InMemoryUserRepository();
        var events = new RecordingInternalEventBus();
        var useCase = new RegisterUserUseCase(users, events);

        var result = useCase.register(RegisterUserCommand.withoutAvatar("OWNER@Example.com", " Owner "));

        assertEquals("owner@example.com", result.user().email().value());
        assertEquals("Owner", result.user().name().value());
        assertFalse(result.user().avatarImage().isPresent());
        assertSame(result.user(), users.savedUser);
        var event = assertInstanceOf(UserRegisteredEvent.class, events.publishedEvent);
        assertEquals(result.user().id(), event.userId());
        assertEquals(result.user().email(), event.email());
    }

    @Test
    void registersUserWithAvatarReference() {
        var useCase = new RegisterUserUseCase(new InMemoryUserRepository(), new RecordingInternalEventBus());
        var avatar = new AvatarImage("avatars/user-1.png", "image/png");

        var result = useCase.register(new RegisterUserCommand("owner@example.com", "Owner", avatar));

        assertEquals(avatar, result.user().avatarImage().orElseThrow());
    }

    @Test
    void rejectsDuplicateEmailBeforePublishingRegistrationEvent() {
        var users = new InMemoryUserRepository();
        users.existingEmail = new EmailAddress("owner@example.com");
        var events = new RecordingInternalEventBus();
        var useCase = new RegisterUserUseCase(users, events);

        var error = assertThrows(
                DuplicateUserEmailException.class,
                () -> useCase.register(RegisterUserCommand.withoutAvatar("owner@example.com", "Owner")));

        assertEquals("owner@example.com", error.email().value());
        assertEquals(0, users.saveCount);
        assertEquals(0, events.publishCount);
    }

    @Test
    void surfacesRepositoryDuplicateEmailAsApplicationError() {
        var users = new InMemoryUserRepository();
        users.failOnSave = true;
        var events = new RecordingInternalEventBus();
        var useCase = new RegisterUserUseCase(users, events);

        assertThrows(
                DuplicateUserEmailException.class,
                () -> useCase.register(RegisterUserCommand.withoutAvatar("owner@example.com", "Owner")));
        assertEquals(0, events.publishCount);
    }

    @Test
    void requiresCollaborators() {
        var users = new InMemoryUserRepository();
        var events = new RecordingInternalEventBus();

        assertThrows(NullPointerException.class, () -> new RegisterUserUseCase(null, events));
        assertThrows(NullPointerException.class, () -> new RegisterUserUseCase(users, null));
        assertThrows(NullPointerException.class, () -> new UserRegisteredEvent(null, new EmailAddress("owner@example.com")));
        assertThrows(NullPointerException.class, () -> new UserRegisteredEvent(users.save(User.create(
                UserId.newId(),
                new EmailAddress("owner@example.com"),
                new UserName("Owner"))).id(), null));
        assertTrue(RegisterUserCommand.withoutAvatar("owner@example.com", "Owner").avatarImageReference().isEmpty());
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<EmailAddress, User> users = new HashMap<>();
        private EmailAddress existingEmail;
        private boolean failOnSave;
        private User savedUser;
        private int saveCount;

        @Override
        public boolean existsByEmail(EmailAddress email) {
            return email.equals(existingEmail) || users.containsKey(email);
        }

        @Override
        public User save(User user) {
            saveCount++;
            if (failOnSave || users.containsKey(user.email())) {
                throw new DuplicateUserEmailException(user.email());
            }
            users.put(user.email(), user);
            savedUser = user;
            return user;
        }
    }

    private static final class RecordingInternalEventBus implements AsynchronousInternalEventBus {
        private InternalEvent publishedEvent;
        private int publishCount;

        @Override
        public void publish(InternalEvent event) {
            publishCount++;
            publishedEvent = event;
        }
    }
}
