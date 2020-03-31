package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.ErrorDecision;
import java.util.Optional;

class EntrySetupParams {
    ErrorDecision errorDecision;
    String key;
    Optional<String> value;
    boolean shouldError;

    public EntrySetupParams(ErrorDecision errorDecision, String key, Optional<String> value, boolean shouldError) {
        this.errorDecision = errorDecision;
        this.key = key;
        this.value = value;
        this.shouldError = shouldError;
    }
}
