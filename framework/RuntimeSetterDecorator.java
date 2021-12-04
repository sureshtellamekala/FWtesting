package net.boigroup.bdd.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RuntimeSetterDecorator  extends CompositeConfiguration{

	private static final Map<String,Object> runtimeConfigurationData = Maps.newConcurrentMap();
	private static final MapConfiguration runtimeConfiguration = new MapConfiguration(runtimeConfigurationData); 
	public RuntimeSetterDecorator( Collection<? extends Configuration> configList ){
		super();
		List<Configuration> configs = Lists.newArrayList((Configuration)runtimeConfiguration);
		configs.addAll(configList);
		for (Configuration c : configs){
			super.addConfiguration(c);
		}
		super.addConfiguration(runtimeConfiguration,true);
	}
	
	@Override
	protected void clearPropertyDirect(String key) {
		super.getInMemoryConfiguration().clearProperty(key);
		runtimeConfigurationData.remove(key);
	}
	
	public void clearProperty(String key){
		super.clearProperty(key);
		runtimeConfigurationData.remove(key);
	}
	@Override
	public void setProperty(String key, Object value){
		super.setProperty(key, value);
		runtimeConfigurationData.put(key, value);
	}
}
