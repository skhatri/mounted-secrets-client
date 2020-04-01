package com.github.skhatri.mounted.model;

import java.util.Optional;

public final class EntryKey {
    private final String namespace;
    private final String key;
    private final Optional<String> defaultValue;

    EntryKey(String namespace, String key, Optional<String> defaultValue) {
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

}
