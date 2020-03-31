package com.github.skhatri.mounted.model;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ProviderList {
    private final List<SecretProvider> providers;

    public ProviderList(List<SecretProvider> providers) {
        this.providers = providers;
    }

    public List<SecretProvider> getProviders() {
        return providers;
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
