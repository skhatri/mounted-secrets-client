package com.github.skhatri.mounted;

import com.github.skhatri.mounted.util.IOUtil;
import java.io.File;

public class FileResourceReader implements ResourceReader {
    @Override
    public String read(String location, boolean failOnError, String defaultValue) {
        File file = new File(location);
        return IOUtil.readFully(file, failOnError, defaultValue);
    }
}
