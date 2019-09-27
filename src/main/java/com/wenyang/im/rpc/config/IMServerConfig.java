package com.wenyang.im.rpc.config;

import com.common.core.constants.BrokerConstants;


public abstract class IMServerConfig {

    public static final String DEFAULT_CONFIG = "classpath:wildfirechat.conf";

    public abstract void setProperty(String name, String value);

    public abstract String getProperty(String name);

    public abstract String getProperty(String name, String defaultValue);

    void assignDefaults() {
        setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(BrokerConstants.PORT));
        setProperty(BrokerConstants.HOST_PROPERTY_NAME, BrokerConstants.HOST);
        setProperty(BrokerConstants.AUTHORIZATOR_CLASS_NAME, "");
    }

    public abstract IResourceLoader getResourceLoader();

}
