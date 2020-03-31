package com.github.skhatri.mounted.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Provider List Tests")
public class ProviderListTest {
    @Test
    @DisplayName("provider list properties")
    public void testProviderListProperties() {
        ProviderList providerList = new ProviderList(new ArrayList<>());
        Assertions.assertTrue(providerList.toMap().isEmpty());

        List<SecretProvider> input = Arrays.asList(
            SecretProviders.any(ErrorDecision.EMPTY)
        );
        ProviderList providerList1 = new ProviderList(input);
        Assertions.assertEquals(input, providerList1.getProviders());
    }
}
