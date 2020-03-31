package com.example;

import org.junit.jupiter.api.BeforeAll;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    protected static String getProperty(String key) {

        return properties.getProperty(key);

    }
}
