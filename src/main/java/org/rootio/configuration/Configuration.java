package org.rootio.configuration;

import java.io.*;
import java.util.Properties;

public class Configuration {
    private Properties properties;

    Configuration(String fileLocation) throws FileNotFoundException {
        try {
            loadProperties(fileLocation);
        } catch (FileNotFoundException e) {
            throw(e);
        }
    }

    private void loadProperties(String fileLocation) throws FileNotFoundException {
        File configFile = new File(fileLocation);
        try (InputStream rdr = new FileInputStream(configFile)) {
            properties.load(rdr);
        } catch (FileNotFoundException e) {
            throw (e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String property)
    {
        return properties.getProperty(property);
    }
}
