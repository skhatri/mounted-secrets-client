package com.github.skhatri.mounted.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Provider List Tests")
public class SecretConfigurationTest {
    @Test
    @DisplayName("provider list properties")
    public void testProviderListProperties() {
        SecretConfiguration providerList = new SecretConfiguration();
        providerList.setProviders(new ArrayList<>());
        Assertions.assertTrue(providerList.toMap().isEmpty());

        List<SecretProvider> input = Arrays.asList(
            SecretProviders.any(ErrorDecision.EMPTY)
        );
        SecretConfiguration providerList1 = new SecretConfiguration();
        providerList1.setProviders(input);
        Assertions.assertEquals(input, providerList1.getProviders());
    }
}
