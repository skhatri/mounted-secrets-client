package com.github.skhatri.mounted.model;

import java.util.Optional;

public class SecretValue {
    public static final SecretValue NOT_FOUND = new SecretValue(Optional.empty(), ValueDecision.NOT_FOUND);

    private final Optional<char[]> value;
    private final ValueDecision decision;

    private SecretValue(Optional<char[]> value, ValueDecision decision) {
        this.decision = decision;
        this.value = value;
    }

    public Optional<char[]> getValue() {
        return value;
    }

    public ValueDecision getDecision() {
        return decision;
    }

    public boolean hasValue() {
        return decision != ValueDecision.NOT_FOUND;
    }

    public boolean isFailure() {
        return decision == ValueDecision.NOT_FOUND;
    }

    public static SecretValue valueOf(Optional<char[]> value) {
        return valueOf(value, ValueDecision.OK);
    }

    public static SecretValue valueOf(Optional<char[]> value, ValueDecision decision) {
        if (decision == ValueDecision.NOT_FOUND || !value.isPresent()) {
            return NOT_FOUND;
        }
        return new SecretValue(value, decision);
    }

}
