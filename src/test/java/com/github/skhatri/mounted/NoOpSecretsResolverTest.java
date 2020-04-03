package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.ErrorDecision;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("No Ops Secret Resolver Tests")
public class NoOpSecretsResolverTest {

    @DisplayName("Resolver returns input as output")
    @Test
    public void testNoOpOutputIsSameAsInput() {
        MountedSecretsResolver resolver = MountedSecretsFactory.noOpResolver();
        Assertions.assertEquals("noop", resolver.name());
        Assertions.assertSame(ErrorDecision.IDENTITY, resolver.errorDecisionStrategy());
        String input = "some::other::value";
        Assertions.assertEquals(Optional.of(input), resolver.resolve(input).getValue().map(chars -> new String(chars)));
    }

}
