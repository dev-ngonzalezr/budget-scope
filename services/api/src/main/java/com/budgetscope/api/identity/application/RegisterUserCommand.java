package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.AvatarImage;
import java.util.Objects;
import java.util.Optional;

public record RegisterUserCommand(String email, String name, AvatarImage avatarImage) {
    public RegisterUserCommand {
        Objects.requireNonNull(email, "Registration email is required");
        Objects.requireNonNull(name, "Registration name is required");
    }

    public static RegisterUserCommand withoutAvatar(String email, String name) {
        return new RegisterUserCommand(email, name, null);
    }

    public Optional<AvatarImage> avatarImageReference() {
        return Optional.ofNullable(avatarImage);
    }
}
