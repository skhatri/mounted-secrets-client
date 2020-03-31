package com.github.skhatri.mounted;

import com.github.skhatri.mounted.model.ErrorDecision;
import com.github.skhatri.mounted.model.MountedSecretsException;
import com.github.skhatri.mounted.model.Pair;
import com.github.skhatri.mounted.model.SecretProvider;
import com.github.skhatri.mounted.model.SecretValue;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemSecretsResolver implements MountedSecretsResolver {
    private final ErrorDecision errorDecision;
    private final ResourceReader resourceReader;
    private final String name;
    private final Map<String, char[]> entries;

    FileSystemSecretsResolver(SecretProvider secretProvider, ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
        this.name = secretProvider.getName();
        this.errorDecision = ErrorDecision.valueOf(secretProvider.getErrorDecision().toUpperCase());
        this.entries = loadEntries(secretProvider);
    }

    private Map<String, char[]> loadEntries(SecretProvider secretProvider) {

        Map<String, char[]> fromMountPath = Optional.ofNullable(secretProvider.getMount()).map(mount -> walker(mount, secretProvider.failOnResourceFailure()))
            .orElseGet(ArrayList::new)
            .stream()
            .flatMap(file -> readFileContent(secretProvider, file))
            .collect(Collectors.toMap(Pair::getKey, vm -> vm.getValue().toCharArray()));

        Map<String, char[]> fromEntriesLocation = Optional.ofNullable(secretProvider.getEntriesLocation())
            .map(fileName -> readPropertiesFile(secretProvider, fileName))
            .orElse(new HashMap<>());

        Map<String, char[]> result = new HashMap<>(fromEntriesLocation);
        result.putAll(fromMountPath);
        return result;
    }

    private Map<String, char[]> readPropertiesFile(SecretProvider secretProvider, String fileName) {
        File file = new File(fileName);
        try {
            Properties props = new Properties();
            props.load(new FileReader(file));
            return props.entrySet()
                .stream()
                .collect(Collectors.toMap(
                    keyMapper -> keyMapper.getKey().toString(),
                    valueMapper -> valueMapper.getValue().toString().toCharArray()
                ));
        } catch (Exception ex) {
            if (!secretProvider.isIgnoreResourceFailure()) {
                throw new MountedSecretsException(String.format("could not read entry location %s", fileName));
            }
            return new HashMap<>();
        }
    }

    private Stream<Pair<String, String>> readFileContent(SecretProvider secretProvider, File file) {
        if (file.isFile()) {
            String data = resourceReader.read(file.getAbsolutePath(), secretProvider.failOnResourceFailure(), "<empty-data>");
            if (!"<empty-data>".equals(data)) {
                String mountPath = new File(secretProvider.getMount()).getAbsolutePath();
                String resourcePath = file.getAbsolutePath();
                String key = resourcePath.replace(String.format("%s/", mountPath), "");
                return Stream.of(new Pair(key, data));
            }
        }
        return Stream.of();
    }


    private List<File> walker(String mountDir, boolean failOnResourceFailure) {
        try (Stream<Path> files = Files.walk(new File(mountDir).toPath())) {
            return files.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException ioe) {
            if (failOnResourceFailure) {
                throw new MountedSecretsException(
                    String.format("issue reading walk directory %s", ioe.getMessage()));
            }
            return new ArrayList<>();
        }
    }

    @Override
    public SecretValue resolve(String key) {
        char[] value = this.entries.get(key);
        return Optional.ofNullable(value)
            .map(entryValue -> SecretValue.valueOf(Optional.of(entryValue)))
            .orElse(SecretValue.NOT_FOUND);
    }

    @Override
    public ErrorDecision errorDecisionStrategy() {
        return errorDecision;
    }

    @Override
    public String name() {
        return this.name;
    }
}
