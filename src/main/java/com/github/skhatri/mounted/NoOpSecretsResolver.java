package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.ErrorDecision;
import com.github.skhatri.mounted.model.SecretValue;
import java.util.Optional;

public class NoOpSecretsResolver implements MountedSecretsResolver {
    @Override
    public String name() {
        return "noop";
    }

    @Override
    public SecretValue resolve(String key) {
        return SecretValue.valueOf(Optional.ofNullable(key.toCharArray()));
    }

    @Override
    public ErrorDecision errorDecisionStrategy() {
        return ErrorDecision.IDENTITY;
    }
}
