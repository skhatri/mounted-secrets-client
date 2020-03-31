package com.github.skhatri.mounted.model;

public final class SecretProviders {

    private SecretProviders() {
    }

    public static SecretProvider any(ErrorDecision errorDecision) {
        return anyForName("vault", errorDecision);
    }

    public static SecretProvider anyForName(String name, ErrorDecision errorDecision) {
        SecretProvider secretProvider = new SecretProvider();
        secretProvider.setMount("sample/secrets");
        secretProvider.setErrorDecision(errorDecision.toString().toLowerCase());
        secretProvider.setName(name);
        secretProvider.setEntriesLocation("sample/all.properties");
        secretProvider.setIgnoreResourceFailure(false);
        return secretProvider;
    }
}
