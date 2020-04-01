package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.ErrorDecision;
import com.github.skhatri.mounted.model.SecretConfiguration;
import com.github.skhatri.mounted.model.SecretProvider;
import com.github.skhatri.mounted.model.SecretProviders;
import com.github.skhatri.mounted.model.SecretValue;
import com.github.skhatri.mounted.model.ValueDecision;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Delegating Mounted Secrets Resolver Test")
public class DelegatingMountedSecretsResolverTest {

    private MountedSecretsResolver secretsResolver;
    private Function<char[], String> charToString = chars -> new String(chars);

    @BeforeEach
    public void setup() {
        SecretProvider vaultProvider = SecretProviders.anyForName("vault", ErrorDecision.EMPTY);
        SecretProvider adhocProvider = SecretProviders.anyForName("vault-identity", ErrorDecision.IDENTITY);
        SecretProvider remoteProvider = SecretProviders.anyForName("vault-fail", ErrorDecision.FAIL);

        SecretConfiguration secretConfiguration = new SecretConfiguration();
        secretConfiguration.setKeyErrorDecision(ErrorDecision.FAIL.toString().toLowerCase());
        secretConfiguration.setProviders(Arrays.asList(vaultProvider, adhocProvider, remoteProvider));
        MountedSecretsFactory factory = new MountedSecretsFactory(secretConfiguration);
        secretsResolver = factory.create();
    }

    @Test
    @DisplayName("delegating secret resolver does not have error decision of it's own")
    public void testDelegatingMountedSecretsErrorDecision() {
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
            new DelegatingMountedSecretsResolver(new HashMap<>(), ErrorDecision.FAIL).errorDecisionStrategy()
        );
    }

    @ParameterizedTest(name = "key error [{index}] with decision {0}")
    @DisplayName("data in unrecognized format are handled as per key error decision")
    @MethodSource("keyErrorData")
    public void testReturnUnrecognizedDataWithoutLookup(ErrorDecision keyErrorDecision, String key, String expectedValue, boolean shouldError) {
        MountedSecretsResolver resolver = new DelegatingMountedSecretsResolver(new HashMap<>(), keyErrorDecision);
        if (shouldError) {
            Assertions.assertThrows(RuntimeException.class, () -> resolver.resolve(key));
        } else {
            SecretValue secretValue = resolver.resolve(key);
            Assertions.assertEquals(ValueDecision.DEFAULT, secretValue.getDecision());
            Assertions.assertEquals(Optional.of(expectedValue), secretValue.getValue().map(s -> new String(s)));
        }
    }

    private static Stream<Arguments> keyErrorData() {
        return Stream.of(
            Arguments.of(ErrorDecision.EMPTY, "", "", false),
            Arguments.of(ErrorDecision.EMPTY, "some::non::existent", "", false),
            Arguments.of(ErrorDecision.IDENTITY, "", "", false),
            Arguments.of(ErrorDecision.IDENTITY, "some::non::existent", "some::non::existent", false),
            Arguments.of(ErrorDecision.FAIL, "", "", true),
            Arguments.of(ErrorDecision.FAIL, "some::non::existent", "", true)
        );
    }


    @Test
    @DisplayName("Must be a delegating resolver")
    public void testFactoryMustCreateADelegatingResolver() {
        Class<?> expected = DelegatingMountedSecretsResolver.class;
        Assertions.assertEquals(expected, secretsResolver.getClass(),
            String.format("expected type is [%s] but found [%s]",
                expected.getName(),
                secretsResolver.getClass().getName()));
        Assertions.assertNotNull(secretsResolver.name());
    }

    @Test
    @DisplayName("errors when namespace is unknown")
    public void testThrowsWhenNamespaceIsUnknown() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> secretsResolver.resolve("secret::xyz::cassandra"));
    }

    @Test
    @DisplayName("load secrets from entries location")
    public void testLoadSecretsFromEntriesLocation() {
        SecretValue secretValue = secretsResolver.resolve("secret::vault-identity::key1");
        Assertions.assertTrue(secretValue.hasValue());

        Assertions.assertEquals(Optional.of("value1"), secretValue.getValue().map(charToString));
    }


    @Test
    @DisplayName("load secrets from mount")
    public void testLoadSecretsFromMount() {
        SecretValue secretValue = secretsResolver.resolve("secret::vault::key4");
        Assertions.assertTrue(secretValue.hasValue());
        Assertions.assertEquals(Optional.of("value4"), secretValue.getValue().map(charToString));
    }


    @Test
    @DisplayName("fallback to default value")
    public void testLoadSecretsToEntryDefaultValue() {
        SecretValue secretValue = secretsResolver.resolve("secret::vault::key1827:-some");
        Assertions.assertTrue(secretValue.hasValue());
        Assertions.assertEquals(Optional.of("some"), secretValue.getValue().map(charToString));

        SecretValue secretValueWithSpace = secretsResolver.resolve("secret::vault::key1827:-some other value!!@");
        Assertions.assertTrue(secretValueWithSpace.hasValue());
        Assertions.assertEquals(Optional.of("some other value!!@"), secretValueWithSpace.getValue().map(charToString));

        SecretValue emptySecretValue = secretsResolver.resolve("secret::vault::key1829:-");
        Assertions.assertTrue(emptySecretValue.hasValue());
        Assertions.assertEquals(Optional.of(""), emptySecretValue.getValue().map(charToString));
    }

    @DisplayName("nested resource keys can be read")
    @Test
    public void testNestedResourceKeys() {
        Assertions.assertEquals(Optional.of("cassandraPassw0rd"),
            secretsResolver.resolve("secret::vault::db/cassandra").getValue().map(charToString));
        Assertions.assertEquals(Optional.of("cassandraPassw0rd"),
            secretsResolver.resolve("secret::vault-fail::db/cassandra").getValue().map(charToString));
        Assertions.assertEquals(Optional.of("Postgres123"),
            secretsResolver.resolve("secret::vault::db/postgres").getValue().map(charToString));
        Assertions.assertEquals(Optional.of("Postgres123"),
            secretsResolver.resolve("secret::vault-fail::db/postgres").getValue().map(charToString));
    }

    @DisplayName("provider error decision is set to fail")
    @Test
    public void testThrowsErrorWhenProviderErrorDecisionIsSetToFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> secretsResolver.resolve("secret::vault-fail::db/cassandra-non-existent"));
        Assertions.assertEquals(Optional.of("default-value"),
            secretsResolver.resolve("secret::vault-fail::db/cassandra-non-existent:-default-value")
                .getValue().map(charToString),
            "should be able to use default password at entry level even for provider whose default is fail");
    }

    @DisplayName("provider error decision is identity")
    @Test
    public void testReturnsKeyAsSecretAlsoWhenErrorDecisionIsIdentity() {
        Assertions.assertEquals(Optional.of("cassandra-non-existent"),
            secretsResolver.resolve("secret::vault-identity::cassandra-non-existent").getValue().map(charToString),
            "should return identity value");
        Assertions.assertEquals(Optional.of("default-value"),
            secretsResolver.resolve("secret::vault-identity::cassandra-non-existent:-default-value").getValue().map(charToString),
            "should be able to use default password at entry level even for provider whose default is identity");
    }


    @DisplayName("provider error decision is empty")
    @Test
    public void testReturnsEmptyValueAsSecretWhenErrorDecisionIsEmpty() {
        Assertions.assertEquals(Optional.of(""),
            secretsResolver.resolve("secret::vault::cassandra-non-existent").getValue().map(charToString),
            "should return empty value");
        Assertions.assertEquals(Optional.of("default-value"),
            secretsResolver.resolve("secret::vault::cassandra-non-existent:-default-value").getValue().map(charToString),
            "should be able to use default password at entry level even for provider whose default is empty");
    }

}
