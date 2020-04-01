package com.github.skhatri.mounted.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class SecretConfiguration {
    private String keyErrorDecision;
    private List<SecretProvider> providers;

    public void setKeyErrorDecision(String keyErrorDecision) {
        this.keyErrorDecision = keyErrorDecision;
    }

    public void setProviders(List<SecretProvider> providers) {
        this.providers = providers;
    }

    public List<SecretProvider> getProviders() {
        return providers;
    }

    public ErrorDecision getKeyErrorDecision() {
        return Optional.ofNullable(keyErrorDecision).map(k -> ErrorDecision.valueOf(k.toUpperCase())).orElse(ErrorDecision.FAIL);
    }

    public final Map<String, SecretProvider> toMap() {
        final UnaryOperator<SecretProvider> identity = UnaryOperator.identity();

        return this.providers.stream()
            .collect(Collectors.toMap(
                SecretProvider::getName,
                identity
            ));
    }
}
