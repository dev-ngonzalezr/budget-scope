package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.User;
import com.budgetscope.api.identity.domain.UserId;
import com.budgetscope.api.identity.domain.UserName;
import com.budgetscope.api.shared.application.AsynchronousInternalEventBus;
import java.util.Objects;

/**
 * Application workflow for registering a user and publishing the registration event through the asynchronous
 * internal event bus. Infrastructure adapters can publish after commit so downstream modules subscribe
 * without coupling registration to their work.
 */
public final class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final AsynchronousInternalEventBus internalEventBus;

    public RegisterUserUseCase(UserRepository userRepository, AsynchronousInternalEventBus internalEventBus) {
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
