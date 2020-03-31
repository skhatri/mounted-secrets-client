package com.github.skhatri.mounted;

class ProviderSetupParams {
    String mountDir;
    String locationsFile;
    boolean ignoreResourceFailure;
    boolean shouldError;

    public ProviderSetupParams(String mountDir, String locationsFile, boolean ignoreResourceFailure, boolean shouldError) {
        this.mountDir = mountDir;
        this.locationsFile = locationsFile;
        this.ignoreResourceFailure = ignoreResourceFailure;
        this.shouldError = shouldError;
    }

}
