package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.User;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.identity.domain.UserName;
import java.util.Objects;

/**
 * Application workflow for registering a user and provisioning their default workspace.
 * Infrastructure adapters should execute {@link #register(RegisterUserCommand)} inside a single transaction
 * so user persistence and workspace provisioning commit or roll back together.
 */
public final class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final WorkspaceProvisioningPort workspaceProvisioningPort;

    public RegisterUserUseCase(UserRepository userRepository, WorkspaceProvisioningPort workspaceProvisioningPort) {
        this.userRepository = Objects.requireNonNull(userRepository, "User repository is required");
        this.workspaceProvisioningPort = Objects.requireNonNull(workspaceProvisioningPort, "Workspace provisioning port is required");
    }

    public RegisteredUserResult register(RegisterUserCommand command) {
        Objects.requireNonNull(command, "Register user command is required");

        var email = new EmailAddress(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserEmailException(email);
        }

        var user = command.avatarImageReference()
                .map(avatarImage -> User.create(UserId.newId(), email, new UserName(command.name()), avatarImage))
                .orElseGet(() -> User.create(UserId.newId(), email, new UserName(command.name())));
        var savedUser = userRepository.save(user);
        var defaultWorkspace = workspaceProvisioningPort.createDefaultWorkspaceFor(savedUser.id());

        return new RegisteredUserResult(savedUser, defaultWorkspace);
    }
}
