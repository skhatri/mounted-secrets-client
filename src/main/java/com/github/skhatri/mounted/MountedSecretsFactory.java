package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.SecretConfiguration;
import com.github.skhatri.mounted.model.SecretProvider;
import java.util.Map;
import java.util.stream.Collectors;

public final class MountedSecretsFactory {


    private final MountedSecretsResolver resolver;

    public MountedSecretsFactory(SecretConfiguration secretConfiguration) {
        Map<String, SecretProvider> providerMap = secretConfiguration.toMap();

        Map<String, MountedSecretsResolver> resolvers = providerMap.entrySet()
            .stream().collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    valueMapper -> new FileSystemSecretsResolver(valueMapper.getValue(), new FileResourceReader())
                )
            );
        this.resolver = new DelegatingMountedSecretsResolver(resolvers, secretConfiguration.getKeyErrorDecision());
    }


    public MountedSecretsResolver create() {
        return resolver;
    }
}
