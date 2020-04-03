package com.github.skhatri.mounted.util;

public final class Preconditions {
    private Preconditions() {
    }

    public static <T> T notNull(T value, String description) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("Precondition FAILED. %s", description));
        }
        return value;
    }

}
