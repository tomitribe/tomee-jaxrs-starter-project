package org.superbiz.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Utility class for loading properties
 * 
 */
public class PropertyUtil
{   
    private PropertyUtil(){
    }

    public static void loadSystemProperties(Properties props)
    {
        if ( props != null )
        {
            for(String key: props.stringPropertyNames())
            {
                key = key.trim();
            	System.setProperty(key, props.getProperty(key));
            }
        }
    }

    /**
     * This method load properties from the specified file path
     *
     * @param propertiesFilePath
     * @return boolean - true if loaded
     */
    public static boolean loadProperties(String name)
    {
        Properties props = new Properties();
        InputStream inputStream = null;
        boolean loaded = false;
        try
        {
            inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(name);

            if (inputStream != null)
            {
                props.load(inputStream);
                loadSystemProperties(props);
                loaded = true;
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return loaded;
    }

    /**
     * This method load properties from the specified file path
     *
     * @param propertiesFilePath
     * @return boolean - true if loaded
     */
    public static boolean loadPropertiesFromFile(String propertiesFilePath)
    {
        Properties props = new Properties();
        InputStream inputStream = null;
        boolean loaded = false;
        try
        {
            File file = new File(propertiesFilePath);

            if ( file.exists() )
            {
            	inputStream = new FileInputStream(new File(propertiesFilePath));

                if (inputStream != null)
                {
                    props.load(inputStream);
                    loadSystemProperties(props);
                    loaded = true;
                }
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return loaded;
    }
}
