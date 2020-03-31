package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.EntryKey;
import com.github.skhatri.mounted.model.ErrorDecision;
import com.github.skhatri.mounted.model.SecretValue;
import com.github.skhatri.mounted.model.ValueDecision;
import java.util.Map;
import java.util.Optional;

public class DelegatingMountedSecretsResolver implements MountedSecretsResolver {

    private final Map<String, MountedSecretsResolver> resolvers;

    DelegatingMountedSecretsResolver(Map<String, MountedSecretsResolver> mountedSecretsResolvers) {
        this.resolvers = mountedSecretsResolvers;
    }

    @Override
    public SecretValue resolve(String key) {
        Optional<EntryKey> entryKeyOpt = EntryKey.fromRawValue(key);
        if (!entryKeyOpt.isPresent()) {
            return SecretValue.valueOf(Optional.of(key.toCharArray()), ValueDecision.DEFAULT);
        }
        return entryKeyOpt.map(entryKey -> {
            MountedSecretsResolver matchingResolver =
                Optional.ofNullable(this.resolvers.get(entryKey.getNamespace()))
                    .orElseThrow(() -> new IllegalArgumentException(String.format("could not find a resolver for namespace %s", entryKey.getNamespace())));

            SecretValue value = matchingResolver.resolve(entryKey.getKey());
            if (value.isFailure()) {
                value = findFallbackValue(entryKey, matchingResolver);
            }
            return value;
        }).orElse(SecretValue.NOT_FOUND);
    }

    private SecretValue findFallbackValue(EntryKey entryKey, MountedSecretsResolver matchingResolver) {
        SecretValue value;
        if (entryKey.getDefaultValue().isPresent()) {
            value = SecretValue.valueOf(entryKey.getDefaultValue().map(String::toCharArray), ValueDecision.DEFAULT);
        } else {
            ErrorDecision errorDecision = matchingResolver.errorDecisionStrategy();
            String errorFallbackValue = errorDecision.handleErrorForKey(entryKey.getKey());
            value = SecretValue.valueOf(Optional.of(errorFallbackValue.toCharArray()), ValueDecision.DEFAULT);
        }
        return value;
    }

    @Override
    public ErrorDecision errorDecisionStrategy() {
        throw new UnsupportedOperationException("non-deterministic decision strategy");
    }

    @Override
    public String name() {
        return "supervisor";
    }
}
