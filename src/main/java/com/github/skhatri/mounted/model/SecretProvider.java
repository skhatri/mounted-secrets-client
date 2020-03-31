package com.github.skhatri.mounted.model;

import java.util.Optional;

public class SecretProvider {
    private String name;
    private String mount;
    private String key;
    private String errorDecision;
    private String entriesLocation;
    private boolean ignoreResourceFailure;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getErrorDecision() {
        return Optional.ofNullable(errorDecision).orElse(ErrorDecision.FAIL.toString().toLowerCase());
    }

    public void setErrorDecision(String errorDecision) {
        this.errorDecision = errorDecision;
    }

    public String getEntriesLocation() {
        return entriesLocation;
    }

    public void setEntriesLocation(String entriesLocation) {
        this.entriesLocation = entriesLocation;
    }

    public boolean isIgnoreResourceFailure() {
        return ignoreResourceFailure;
    }

    public boolean failOnResourceFailure() {
        return !ignoreResourceFailure;
    }

    public void setIgnoreResourceFailure(boolean ignoreResourceFailure) {
        this.ignoreResourceFailure = ignoreResourceFailure;
    }
}
