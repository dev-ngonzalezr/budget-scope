package com.budgetscope.api.identity.domain;

import java.util.Objects;
import java.util.Optional;

public final class User {
    private final UserId id;
    private EmailAddress email;
    private UserName name;
    private AvatarImage avatarImage;

    public User(UserId id, EmailAddress email, UserName name, AvatarImage avatarImage) {
        this.id = Objects.requireNonNull(id, "User id is required");
        this.email = Objects.requireNonNull(email, "Email address is required");
        this.name = Objects.requireNonNull(name, "User name is required");
        this.avatarImage = avatarImage;
    }

    public static User create(UserId id, EmailAddress email, UserName name) {
        return new User(id, email, name, null);
    }

    public static User create(UserId id, EmailAddress email, UserName name, AvatarImage avatarImage) {
        return new User(id, email, name, avatarImage);
    }

    public UserId id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public UserName name() {
        return name;
    }

    public Optional<AvatarImage> avatarImage() {
        return Optional.ofNullable(avatarImage);
    }

    public void changeEmail(EmailAddress newEmail) {
        email = Objects.requireNonNull(newEmail, "Email address is required");
    }

    public void changeName(UserName newName) {
        name = Objects.requireNonNull(newName, "User name is required");
    }

    public void changeAvatar(AvatarImage newAvatarImage) {
        avatarImage = Objects.requireNonNull(newAvatarImage, "Avatar image is required");
    }

    public void removeAvatar() {
        avatarImage = null;
    }
}
