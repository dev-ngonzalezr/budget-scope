package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.User;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.identity.domain.UserName;
import com.budgetscope.api.shared.application.InternalEventBus;
import java.util.Objects;

/**
 * Application workflow for registering a user and publishing the registration event.
 * Infrastructure adapters should execute {@link #register(RegisterUserCommand)} and synchronous internal event
 * handlers inside a single transaction so user persistence and default workspace provisioning commit or roll back together.
 */
public final class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final InternalEventBus internalEventBus;

    public RegisterUserUseCase(UserRepository userRepository, InternalEventBus internalEventBus) {
        this.userRepository = Objects.requireNonNull(userRepository, "User repository is required");
        this.internalEventBus = Objects.requireNonNull(internalEventBus, "Internal event bus is required");
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
        internalEventBus.publish(new UserRegisteredEvent(savedUser.id(), savedUser.email()));

        return new RegisteredUserResult(savedUser);
    }
}
