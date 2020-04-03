package com.example;

import org.junit.jupiter.api.BeforeAll;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertyReader {

    private static Properties properties;

    @BeforeAll
    public static void initializeProperties() throws FileNotFoundException, IOException {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "application.properties";

        properties = new Properties();
        properties.load(new FileInputStream(appConfigPath));

    }

    public static String resolveRelativeResourcesPath(String relativeresourcePath) {

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        Path resolvedPath = Paths.get(rootPath, relativeresourcePath);
        return resolvedPath.toAbsolutePath().toString();
    }

    protected static String getProperty(String key) {

        return properties.getProperty(key);

    }

    protected static String getProperty(String key, String defaultValue) {

        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }
}
