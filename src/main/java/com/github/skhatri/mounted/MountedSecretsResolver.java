package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.ErrorDecision;
import com.github.skhatri.mounted.model.SecretValue;

public interface MountedSecretsResolver {
    String name();

    SecretValue resolve(String key);

    ErrorDecision errorDecisionStrategy();
}
