package net.boigroup.bdd.framework;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;

import com.google.common.io.Resources;
import org.apache.log4j.Logger;

public class ConfigLoader {

	private static CompositeConfiguration config;
	public static final String PROPS = "test_props";
	public static Configuration config(){
		return getConfig();
	}

	final static Logger LOG = Logger.getLogger(ConfigLoader.class);

	private static Configuration getConfig(){
		if (config == null){
			PropertiesConfiguration props = new PropertiesConfiguration();
			try {
				File propertiesFirectory = new File(Resources.getResource(PROPS).toURI());
				props = listFilesAndFilesSubDirectories(propertiesFirectory.getAbsolutePath(), props);
			} catch (IllegalArgumentException | URISyntaxException  e) {
				LOG.error("Error loading configuration", e);
			}

			config = new RuntimeSetterDecorator(
					Arrays.asList(
							new SystemConfiguration(),
							new EnvironmentConfiguration(),
							props
					));
			for (int i =0;i<config.getNumberOfConfigurations();i++){
				//logConfig(config.getConfiguration(i));

			}
		}

		return config;
	}

	private static PropertiesConfiguration listFilesAndFilesSubDirectories(String directoryName, PropertiesConfiguration props){
		File directory = new File(directoryName);
		File[] fList = directory.listFiles();
		for (File propFile : fList){
			if (propFile.isFile()){
				try {
					props.load(propFile);
					LOG.debug("Properties loaded from file { " + propFile + " }");
				} catch (ConfigurationException e) {
					LOG.error("Error loading file { "+propFile+" } {" + e + "}");
				}
			} else if (propFile.isDirectory()){
				listFilesAndFilesSubDirectories(propFile.getAbsolutePath(), props);
			}
		}

		return props;
	}

	public static void addEventListener(ConfigurationListener listener){
		((EventSource)getConfig()).addConfigurationListener(listener);
	}
}
