package com.budgetscope.api.identity.domain;

import java.util.Objects;

public record AvatarImage(String storageKey, String contentType) {
    private static final int MAX_STORAGE_KEY_LENGTH = 512;
    private static final int MAX_CONTENT_TYPE_LENGTH = 128;

    public AvatarImage {
        Objects.requireNonNull(storageKey, "Avatar storage key is required");
        Objects.requireNonNull(contentType, "Avatar content type is required");
        storageKey = storageKey.trim();
        contentType = contentType.trim().toLowerCase();
        if (storageKey.isBlank()) {
            throw new IllegalArgumentException("Avatar storage key is required");
        }
        if (storageKey.length() > MAX_STORAGE_KEY_LENGTH) {
            throw new IllegalArgumentException("Avatar storage key must be at most " + MAX_STORAGE_KEY_LENGTH + " characters");
        }
        if (contentType.isBlank()) {
            throw new IllegalArgumentException("Avatar content type is required");
        }
        if (contentType.length() > MAX_CONTENT_TYPE_LENGTH) {
            throw new IllegalArgumentException("Avatar content type must be at most " + MAX_CONTENT_TYPE_LENGTH + " characters");
        }
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Avatar content type must be an image media type");
        }
    }
}
