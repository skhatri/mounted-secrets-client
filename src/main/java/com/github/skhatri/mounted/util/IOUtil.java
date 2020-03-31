package com.github.skhatri.mounted.util;

import com.github.skhatri.mounted.model.ResourceException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class IOUtil {

    private IOUtil() {

    }

    public static String readFully(File file) {
        return readFully(file, true, null);
    }


    public static String readFully(File file, boolean failOnError, String defaultValue) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            if (failOnError) {
                throw new ResourceException(String.format("could not read file %s reason: %s", file.getName(), ioe.getMessage()));
            }
            return defaultValue;
        }
    }
}
