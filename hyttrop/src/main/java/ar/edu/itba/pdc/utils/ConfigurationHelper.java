package ar.edu.itba.pdc.utils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class ConfigurationHelper {

    private static Logger logger = Logger.getLogger(ConfigurationHelper.class);
    private static ConfigurationHelper INSTANCE;
    private final Configuration config;

    private ConfigurationHelper(){
        try{
            config = new XMLConfiguration("src/main/resources/hyttrop.config");
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
}
