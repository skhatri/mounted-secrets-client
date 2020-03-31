package com.github.skhatri.mounted;


public interface ResourceReader {
    String read(String location, boolean failOnError, String defaultValue);
}
