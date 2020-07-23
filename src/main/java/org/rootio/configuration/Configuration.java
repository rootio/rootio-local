package org.rootio.configuration;

import java.io.*;
import java.util.Properties;

public class Configuration {
    private static Properties properties;

    Configuration(String fileLocation) throws FileNotFoundException {
        try {
            loadProperties(fileLocation);
        } catch (FileNotFoundException e) {
            throw(e);
        }
    }

    private static void loadProperties(String fileLocation) throws FileNotFoundException {
        File configFile = new File(fileLocation);
        try (InputStream rdr = new FileInputStream(configFile)) {
            properties.load(rdr);
        } catch (FileNotFoundException e) {
            throw (e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String property)
    {
        return properties.getProperty(property);
    }

    public static String getProperty(String property, String defaultValue)
    {
        return properties.getProperty(property, defaultValue);
    }

    public static void setProperty(String k, String v) {
        properties.setProperty(k, v);
    }

}
