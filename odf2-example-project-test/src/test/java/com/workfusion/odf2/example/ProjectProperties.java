package com.workfusion.odf2.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

class ProjectProperties {

    private final Properties properties;

    ProjectProperties() {
        this("project.properties");
    }

    ProjectProperties(String resourceName) {
        properties = readProperties(resourceName);
    }

    String getProjectVersion() {
        return properties.getProperty("project.version");
    }

    String getPackageModuleName() {
        return properties.getProperty("package.module.name");
    }

    String getBundlePath() {
        String packageModuleName = getPackageModuleName();
        return String.format("../%s/target/%s-%s.zip", packageModuleName, packageModuleName, getProjectVersion());
    }

    private static Properties readProperties(String resourceName) {
        try (InputStream inputStream = ProjectProperties.class.getClassLoader().getResourceAsStream(resourceName)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
