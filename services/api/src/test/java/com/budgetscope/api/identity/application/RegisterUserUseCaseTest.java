package com.budgetscope.api.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.budgetscope.api.identity.domain.AvatarImage;
import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.User;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.workspace.domain.Workspace;
import com.budgetscope.api.workspace.domain.WorkspaceId;
import com.budgetscope.api.workspace.domain.WorkspaceName;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class RegisterUserUseCaseTest {
    @Test
    void registersUserAndProvisionsDefaultWorkspace() {
        var users = new InMemoryUserRepository();
        var workspaces = new RecordingWorkspaceProvisioningPort();
        var useCase = new RegisterUserUseCase(users, workspaces);

        var result = useCase.register(RegisterUserCommand.withoutAvatar("OWNER@Example.com", " Owner "));

        assertEquals("owner@example.com", result.user().email().value());
        assertEquals("Owner", result.user().name().value());
        assertFalse(result.user().avatarImage().isPresent());
        assertSame(result.user(), users.savedUser);
        assertEquals(result.user().id(), workspaces.provisionedOwnerId);
        assertEquals("default", result.defaultWorkspace().name().value());
        assertEquals(result.user().id(), result.defaultWorkspace().ownerId());
    }

    @Test
    void registersUserWithAvatarReference() {
        var useCase = new RegisterUserUseCase(new InMemoryUserRepository(), new RecordingWorkspaceProvisioningPort());
        var avatar = new AvatarImage("avatars/user-1.png", "image/png");

        var result = useCase.register(new RegisterUserCommand("owner@example.com", "Owner", avatar));

        assertEquals(avatar, result.user().avatarImage().orElseThrow());
    }

    @Test
    void rejectsDuplicateEmailBeforeProvisioningWorkspace() {
        var users = new InMemoryUserRepository();
        users.existingEmail = new EmailAddress("owner@example.com");
        var workspaces = new RecordingWorkspaceProvisioningPort();
        var useCase = new RegisterUserUseCase(users, workspaces);

        var error = assertThrows(
                DuplicateUserEmailException.class,
                () -> useCase.register(RegisterUserCommand.withoutAvatar("owner@example.com", "Owner")));

        assertEquals("owner@example.com", error.email().value());
        assertEquals(0, users.saveCount);
        assertEquals(0, workspaces.provisionCount);
    }

    @Test
    void surfacesRepositoryDuplicateEmailAsApplicationError() {
        var users = new InMemoryUserRepository();
        users.failOnSave = true;
        var useCase = new RegisterUserUseCase(users, new RecordingWorkspaceProvisioningPort());

        assertThrows(
                DuplicateUserEmailException.class,
                () -> useCase.register(RegisterUserCommand.withoutAvatar("owner@example.com", "Owner")));
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

    private static final class RecordingWorkspaceProvisioningPort implements WorkspaceProvisioningPort {
        private UserId provisionedOwnerId;
        private int provisionCount;

        @Override
        public Workspace createDefaultWorkspaceFor(UserId ownerId) {
            provisionCount++;
            provisionedOwnerId = ownerId;
            return Workspace.create(WorkspaceId.newId(), new WorkspaceName("default"), ownerId);
        }
    }
}
