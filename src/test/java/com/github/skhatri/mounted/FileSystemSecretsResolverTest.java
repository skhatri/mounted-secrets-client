package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.ErrorDecision;
import com.github.skhatri.mounted.model.MountedSecretsException;
import com.github.skhatri.mounted.model.SecretProvider;
import com.github.skhatri.mounted.model.SecretProviders;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@DisplayName("File System Secrets Resolver Tests")
public class FileSystemSecretsResolverTest {

    @DisplayName("ignore entries when resource ignore is on but file's cant be read")
    @Test
    public void testResourceErrorWithIgnoreFlagOn() {
        SecretProvider provider = SecretProviders.anyForName(
            "accept-resource-errors-vault", ErrorDecision.EMPTY);
        provider.setIgnoreResourceFailure(true);
        ResourceReader mockFileReader = Mockito.mock(ResourceReader.class);
        Mockito.when(mockFileReader.read(Mockito.anyString(), Mockito.eq(false), Mockito.eq("<empty-data>"))).thenReturn("<empty-data>");
        FileSystemSecretsResolver resolver = new FileSystemSecretsResolver(provider, mockFileReader);
        Assertions.assertEquals(Optional.empty(), resolver.resolve("db/postgres").getValue().map(chars -> new String(chars)));
        Mockito.verify(mockFileReader, Mockito.atLeast(1)).read(Mockito.anyString(), Mockito.eq(false), Mockito.eq("<empty-data>"));

    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("data")
    public void testFileSystemSecretsConfiguration(String testName,
                                                   ProviderSetupParams providerParams,
                                                   List<EntrySetupParams> entryParams) {
        entryParams.forEach(entryParam -> {
            ErrorDecision errorDecision = entryParam.errorDecision;
            String providerName = String.format("vault-%s", errorDecision.toString().toLowerCase());
            SecretProvider provider = SecretProviders.anyForName(
                providerName, errorDecision);
            provider.setMount(providerParams.mountDir);
            provider.setEntriesLocation(providerParams.locationsFile);
            provider.setIgnoreResourceFailure(providerParams.ignoreResourceFailure);
            if (providerParams.shouldError) {
                Assertions.assertThrows(MountedSecretsException.class, () -> new FileSystemSecretsResolver(provider, new FileResourceReader()));
            } else {
                MountedSecretsResolver resolver = new FileSystemSecretsResolver(provider, new FileResourceReader());
                Assertions.assertEquals(providerName, resolver.name());
                if (entryParam.shouldError) {
                    Assertions.assertThrows(MountedSecretsException.class, () -> resolver.resolve(entryParam.key));
                } else {
                    Assertions.assertEquals(entryParam.value,
                        resolver.resolve(entryParam.key).getValue().map(chars -> new String(chars)),
                        String.format("with error decision [%s] lookup of [%s] expected [%s]", errorDecision.toString().toLowerCase(), entryParam.key, entryParam.value));
                }
            }
        });

    }

    private static Stream<Arguments> data() {
        return Stream.of(
            validConfig(),
            invalidResourcePaths("should resolve when mount is wrong but ignore resource exception is true",
                "sample/secrets2", "sample/all.properties", Optional.empty(), Optional.of("value1")),
            invalidResourcePaths("should resolve when location is wrong but ignore resource exception is true",
                "sample/secrets", "sample/allx23.properties", Optional.of("Postgres123"), Optional.empty()),
            invalidResourcePaths("should resolve when mount and location are wrong but ignore resource exception is true",
                "sample/secrets2", "sample/allxx2.properties", Optional.empty(), Optional.empty()),
            Arguments.of("should error when all mount is wrong and ignore resource is false",
                new ProviderSetupParams("sample/2secrets", "sample/all.properties", false, true),
                Arrays.asList(
                    new EntrySetupParams(ErrorDecision.EMPTY, "db/postgres", Optional.of("N/A"), true)
                )
            ),
            Arguments.of("should error when all location is wrong and ignore resource is false",
                new ProviderSetupParams("sample/secrets", "sample/allx23.properties", false, true),
                Arrays.asList(
                    new EntrySetupParams(ErrorDecision.EMPTY, "db/postgres", Optional.of("N/A"), true)
                )
            )
        );
    }

    private static Arguments invalidResourcePaths(String s, String s2, String s3, Optional<String> mountKeyValue, Optional<String> locationKeyValue) {
        return Arguments.of(s,
            new ProviderSetupParams(s2, s3, true, false),
            Arrays.asList(
                new EntrySetupParams(ErrorDecision.EMPTY, "db/postgres", mountKeyValue, false),
                new EntrySetupParams(ErrorDecision.EMPTY, "key1", locationKeyValue, false),
                new EntrySetupParams(ErrorDecision.IDENTITY, "db/postgres", mountKeyValue, false),
                new EntrySetupParams(ErrorDecision.IDENTITY, "key1", locationKeyValue, false),
                new EntrySetupParams(ErrorDecision.FAIL, "db/postgres", mountKeyValue, false),
                new EntrySetupParams(ErrorDecision.FAIL, "key1", locationKeyValue, false))
        );
    }

    private static Arguments validConfig() {
        return Arguments.of("should resolve when config is correct",
            new ProviderSetupParams("sample/secrets", "sample/all.properties", true, false),
            Arrays.asList(new EntrySetupParams(ErrorDecision.EMPTY, "db/postgres", Optional.of("Postgres123"), false),
                new EntrySetupParams(ErrorDecision.IDENTITY, "db/postgres", Optional.of("Postgres123"), false),
                new EntrySetupParams(ErrorDecision.FAIL, "db/postgres", Optional.of("Postgres123"), false))
        );
    }

}
