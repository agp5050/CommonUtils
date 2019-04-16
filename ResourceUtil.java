package com.te.datalake.util;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.te.datalake.entity.QuartzTaskOnAll;
import com.te.datalake.service.ApolloCorelationInit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class ResourceUtil {

	private final static Logger logger = LoggerFactory.getLogger(ResourceUtil.class);
	private  static String localPath = ResourceUtil.class.getResource("/").getPath();
	
	private  static Config application =null;
	private  static Config querytable = null;
	
	private static Properties props = null;
	
	private static List<ApolloCorelationInit> theAppsUseTheProps=new ArrayList<>();

	public static void addToWatch(ApolloCorelationInit item){
		theAppsUseTheProps.add(item);
	}
	
// ------------------------- 从apollo中加载配置信息 begin -----------------------	
//	注意同时修改getProperties()方法，
	public static void initApolloConfg(){


		logger.info("apollo config loading begin -------------");
		props = new Properties();
				
		application =ConfigService.getAppConfig();
		querytable = ConfigService.getConfig("JFDCRT.ORIGINAL_DATA_TABLE");
		
		querytable.getPropertyNames().stream().forEach(key -> {
			props.put(key, querytable.getProperty(key, "").replaceAll(" ", ""));
			logger.info("querytable:"+key+":"+querytable.getProperty(key, ""));
		});
		//apollo配置更改
        ConfigChangeListener configChangeListener = changeEvent -> {
            for (String key : changeEvent.changedKeys()) {
                ConfigChange change = changeEvent.getChange(key);
                logger.warn("apollo config is changed for namespace: {}",change.getNamespace());
                if (change.getChangeType() == PropertyChangeType.DELETED) {
                    props.remove(key);
                } else {
                    props.put(key, change.getNewValue());
                    QuartzTaskOnAll.loadConfigFromPro(props, key);
                }
                logger.info("changetype:" + change.getChangeType() + "," + key + ":" + application.getProperty(key, ""));
                logger.info("properties : {}",props);
            }
            //更新加载props这个配置的其他配置container
            for (ApolloCorelationInit item : theAppsUseTheProps) {
                item.reInit();
            }
        };
        querytable.addChangeListener(configChangeListener);
		
		application.getPropertyNames().stream().forEach(key -> {
			props.put(key, application.getProperty(key, "").replaceAll(" ", ""));
			logger.info("application:"+key+":"+application.getProperty(key, ""));
			QuartzTaskOnAll.loadConfigFromPro(props,key);
		});
		application.addChangeListener(configChangeListener);
		logger.info("apollo config loading end -------------");
	}

    private static void testInit() {
        Map<String, Object> stringObjectMap = loadPropertiesToMap("config.properties");
        props=new Properties();
        props.put("mysql.driver",stringObjectMap.get("mysql.driver"));
        props.put("mysql.url",stringObjectMap.get("mysql.url"));
        props.put("mysql.username",stringObjectMap.get("mysql.username"));
        props.put("mysql.password",stringObjectMap.get("mysql.password"));
    }
// ------------------------- 从apollo中加载配置信息 end -----------------------	
	

	public static Properties getProperties() {
		if(props == null || props.isEmpty()){
//			init(configFileName);   //从配置文件加载配置信息
			synchronized (ResourceUtil.class){
				if (props == null || props.isEmpty()){
					initApolloConfg();         //从apollo中加载配置信息
				}
			}
//            testInit();
		}
		return props;
	}

	public static int getInt(String name) {
		return Integer.parseInt(getProperties().getProperty(name));
	}
	public static long getLong(String name) {
		return Long.parseLong(getProperties().getProperty(name));
	}

	public static String getString(String name) {
		return getProperties().getProperty(name);
	}

	public static String getString(String name, String defaultValue) {
		return getProperties().getProperty(name, defaultValue);
	}
	//Local resource load program......
	public static Map<String,Object> loadPropertiesToMap(String path) {
		if (!StringUtils.isBlank(path)) {
			Reader reader = null;
			try {
				Properties properties = new Properties();
				reader = new InputStreamReader(new FileInputStream(localPath + path), "UTF-8");
				properties.load(reader);
				Map<String, Object> map = new HashMap<>();
				for (String key : properties.stringPropertyNames()) {
					Object value = properties.getProperty(key).trim();
					map.put(key, value);
				}
				return map;
			} catch (IOException e) {
				logger.error("Load properties error : " + e.getMessage(), e);
				throw new RuntimeException(e);
			}finally {

				try {
					if (reader!=null) {
						reader.close();
					}
				} catch (IOException e) {
					logger.error("close file error : " + e.getMessage(), e);
				}
			}
		} else {
			logger.error("Properties path should not be empty.",path);
			throw new RuntimeException();
		}
		//return null;
	}

	
	public static void main(String[] args) {
//		String path = "file:/app/pro/dc_invoke/dc_invoke-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/";	    	
//		String jarPath = path.substring(0, path.indexOf(".jar"));
//    	
//    	logger.info("jarPath="+jarPath+" ,last end " + localPath.lastIndexOf("/"));
//    	
//    	localPath = jarPath.substring(5, jarPath.lastIndexOf("/"))+"/config/";
//    	
//    	logger.info("localPath="+localPath);
        Map<String, Object> stringObjectMap = ResourceUtil.loadPropertiesToMap("config.properties");
        System.out.println(stringObjectMap);
        String str = "";
		String bl = "";
		System.out.println(str.indexOf(bl));
	}
}

public interface ApolloCorelationInit{
  void reInit();
}
