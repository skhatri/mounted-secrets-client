package com.github.skhatri.mounted.model;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Secret Value Tests")
public class SecretValueTest {

    @DisplayName("secret value contract test")
    @Test
    public void testSecretValueConstructionAndProperties() {
        SecretValue secretValue = SecretValue.valueOf(Optional.empty());
        Assertions.assertEquals(SecretValue.NOT_FOUND, secretValue);
        Assertions.assertFalse(secretValue.hasValue());

        SecretValue testValue = SecretValue.valueOf(Optional.of("test").map(String::toCharArray));
        Assertions.assertTrue(testValue.hasValue());
        Assertions.assertEquals(Optional.of("test"), testValue.getValue().map(chars -> new String(chars)));

        SecretValue misConfiguredSecretValue = SecretValue.valueOf(Optional.empty(), ValueDecision.OK);
        Assertions.assertTrue(misConfiguredSecretValue.isFailure());
        Assertions.assertEquals(ValueDecision.NOT_FOUND, misConfiguredSecretValue.getDecision());

        Assertions.assertEquals(SecretValue.NOT_FOUND, SecretValue.valueOf(Optional.empty(), ValueDecision.NOT_FOUND));

    }
}
