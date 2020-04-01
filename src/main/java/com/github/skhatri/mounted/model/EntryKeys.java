package com.github.skhatri.mounted.model;

import java.util.Optional;

public final class EntryKeys {

    private EntryKeys() {
    }

    public static Optional<EntryKey> fromRawValue(String value) {
        String[] parts = value.split("::");
        if (parts.length < 3 || !"secret".equalsIgnoreCase(parts[0])) {
            return Optional.empty();
        }
        String namespace = parts[1];
        String keyWithValue = parts[2];
        String[] keyValue = keyWithValue.split(":-");
        Optional<String> defaultValue;
        if (keyValue.length == 1) {
            defaultValue = Optional.empty();
        } else {
            defaultValue = Optional.of(keyValue[1].trim());
        }
        return Optional.of(new EntryKey(namespace, keyValue[0], defaultValue));
    }
}
