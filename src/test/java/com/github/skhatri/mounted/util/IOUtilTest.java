package com.github.skhatri.mounted.util;

import com.github.skhatri.mounted.model.ResourceException;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IO Util Tests")
public class IOUtilTest {
    @Test
    @DisplayName("read existing file")
    public void testReadExistingFile() {
        Assertions.assertEquals("value4", IOUtil.readFully(new File("sample/secrets/key4")));
    }

    @Test
    @DisplayName("error on non-existent file")
    public void testFailsWithResourceExceptionForInvalidFiles() {
        Assertions.assertThrows(ResourceException.class, () -> IOUtil.readFully(new File("sample/secrets/key-3393")));
    }

    @Test
    @DisplayName("skip error on non-existent file with default value")
    public void testSkipFailureOnNonExistentWithDefaultValue() {
        String content = IOUtil.readFully(new File("sample/secrets/key-3393"), false, "test123");
        Assertions.assertEquals("test123", content);
    }
}
