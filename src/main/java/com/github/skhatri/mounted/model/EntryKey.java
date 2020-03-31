package com.github.skhatri.mounted.model;

import java.util.Optional;

public final class EntryKey {
    private final String namespace;
    private final String key;
    private final Optional<String> defaultValue;

    private EntryKey(String namespace, String key, Optional<String> defaultValue) {
        this.namespace = namespace;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKey() {
        return key;
    }

    public Optional<String> getDefaultValue() {
        return defaultValue;
    }

    public static Optional<EntryKey> fromRawValue(String value) {
        String[] parts = value.split("::");
        if (parts.length < 3) {
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
