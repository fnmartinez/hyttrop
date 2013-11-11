package ar.edu.itba.pdc.utils;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class ConfigurationHelper {

    private static final int DEFAULT_PROXY_PORT = 8080;
    private static final int DEFAULT_ADMIN_PORT = 2345;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 6;
    private static Logger logger = Logger.getLogger(ConfigurationHelper.class);
    private static ConfigurationHelper INSTANCE;
    private final Configuration config;

    private int proxyPort;
    private int adminPort;
    private int bufferSize;

    private ConfigurationHelper(){
        try{
            config = new XMLConfiguration("/home/facundo/workspace/pdc2013b/hyttrop/src/main/resources/hyttrop.config");
            try {
                proxyPort = config.getInt("proxy.port", DEFAULT_PROXY_PORT);
            } catch (ConversionException ce) {
                proxyPort = DEFAULT_PROXY_PORT;
            }
            try {
                adminPort = config.getInt("adminServer.port", DEFAULT_ADMIN_PORT);
            } catch (ConversionException ce) {
                adminPort = DEFAULT_ADMIN_PORT;
            }
            try {
                bufferSize = config.getInt("thorium.buffer.size", DEFAULT_BUFFER_SIZE);
            } catch (ConversionException ce) {
                bufferSize = DEFAULT_BUFFER_SIZE;
            }
        }catch(ConfigurationException e){
            logger.fatal("An error occurred while fetching configuration from file.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static synchronized ConfigurationHelper getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ConfigurationHelper();
        }
        return INSTANCE;
    }

    public int getProxyPort(){
        return config.getInt("proxy.port");
    }

    public int getAdministratorPort(){
        return config.getInt("adminServer.port");
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
