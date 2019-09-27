package com.wenyang.im.rpc.config;

import java.util.Map;
import java.util.Properties;

/**
 * Configuration backed by memory.
 */
public class MemoryConfig extends IMServerConfig {

    private final Properties m_properties = new Properties();

    public MemoryConfig(Properties properties) {
        assignDefaults();
        for (Map.Entry<Object, Object> entrySet : properties.entrySet()) {
            m_properties.put(entrySet.getKey(), entrySet.getValue());
        }
    }


    @Override
    public void setProperty(String name, String value) {
        m_properties.setProperty(name, value);
    }

    @Override
    public String getProperty(String name) {
        return m_properties.getProperty(name);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return m_properties.getProperty(name, defaultValue);
    }

    @Override
    public IResourceLoader getResourceLoader() {
        return new FileResourceLoader();
    }

}
