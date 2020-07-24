package org.rootio.configuration;

import java.io.*;
import java.util.Properties;

public class Configuration {
    private static Properties properties;

    Configuration(String fileLocation) throws FileNotFoundException {
        properties = new Properties();
        loadProperties(fileLocation);
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

    public static boolean setProperty(String k, String v) {
        boolean isChanged = properties.containsKey(k) && properties.get(k) != v;
        properties.setProperty(k, v);
        return isChanged;
    }

    public static boolean isSet(String property)
    {
        return properties.containsKey(property);
    }
}
