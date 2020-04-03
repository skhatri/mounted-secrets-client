package com.github.skhatri.mounted.util;

import com.github.skhatri.mounted.model.EntryKey;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Preconditions Test")
public class PreconditionsTest {

    @DisplayName("check require not null")
    @Test
    public void testCheckNotNull() {
        Assertions.assertEquals("", Preconditions.notNull("", "should not be null"));
        Map<String, Object> map = new HashMap<>();
        Assertions.assertEquals(map, Preconditions.notNull(map, "should not be null"));
    }

    @DisplayName("check require null failure scenario")
    @Test
    public void testCheckNotNullFailsForNullValues() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Preconditions.notNull(null, "expected to be not null"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Preconditions.notNull((EntryKey) null, "expected to be not null"));
    }
}
