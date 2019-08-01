package org.llw.com.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/***
 * 
 * @author liulw 动态获取参数的处理
 */
@Slf4j
public class PropertiesExtUtils {

	private static Map<String, String> propertiesMap = new HashMap<String, String>();
	/** 默认的工程文件 */
	private static String KEY_application = "application";
	/** 文件后缀名 */
	private static String KEY_properties = ".properties";
	/** 需要加载的其他配置文件名 */
	private static String KEY_otherConfigs = "otherConfigs";

	private static class InnerClassSingleton {
		private final static PropertiesExtUtils  pro = new PropertiesExtUtils();
	 }
	
	public static PropertiesExtUtils getInstance(){
		return InnerClassSingleton.pro;
	}

	private PropertiesExtUtils() {
		Properties props = null;
		String profileActive = null;
		try {
			props = loadPropertiesFile(KEY_application + KEY_properties);
			if (props != null) {
				profileActive = props.getProperty("spring.profiles.active");
			}
			// 加载application参数
			processProperties(props);
			log.info("spring.profiles.active:" + profileActive);
		} catch (Exception e) {
			log.error("get profileActive error:",e);
		}
		// 没有配置文件
		if (profileActive == null) {
			if (props != null) {
				processProperties(props);
			}
		} else {
			Properties devProps = loadPropertiesFile(KEY_application + "-" + profileActive + KEY_properties);
			log.info("loan :" + profileActive);
			PropertiesExtUtils.processProperties(devProps);
		}
		String otherConfigs = null;
		try {
			otherConfigs = propertiesMap.get(PropertiesExtUtils.KEY_otherConfigs);
			if (otherConfigs == null)
				return;
			for (String config : otherConfigs.split(",")) {
				try {
					props = loadPropertiesFile(config + KEY_properties);
					PropertiesExtUtils.processProperties(props);
				} catch (Exception e) {
					log.error("loadn Properties error:" + config,e);
				}
			}
		} catch (Exception e) {
			log.error("get otherConfigs error:");
		}

	}

	private static void processProperties(Properties props) {
		if (props == null) {
			return;
		}
		for (Object key : props.keySet()) {
			String keyStr = key.toString();
			try {
				// PropertiesLoaderUtils的默认编码是ISO-8859-1,在这里转码一下
				String value = StringUtil.converCodeToUTF8(props.getProperty(key.toString()));
				propertiesMap.put(keyStr, value);
			} catch (Exception e) {
				log.error("processProperties error:",e);
			}
		}
	}

	/***
	 * 加载文件信息
	 * 
	 * @param propertyFileName
	 */
	public Properties loadPropertiesFile(String propertyFileName) {
		try {
			Properties pro = new Properties();
			InputStream in = getResource(propertyFileName);
			pro.load(in);
			in.close();
			return pro;
		} catch (Exception e) {
			log.error("loadn Properties error:" + propertyFileName,e);
			return null;
		}
	}

	/***
	 * 加载根目录下文件信息,全路径加载
	 * 
	 * @param fileUrl
	 */
	public static InputStream getResource(String fileUrl) {
		try {
			if (fileUrl.startsWith("/")) {
				Resource resource = new DefaultResourceLoader().getResource("classpath:" + fileUrl);
				return resource.getInputStream();
			}
			Resource resource = new DefaultResourceLoader().getResource("classpath:/" + fileUrl);
			return resource.getInputStream();
		} catch (Exception e) {
			log.error("loadn Properties error:" + fileUrl,e);
			return null;
		}
	}

	/***
	 * 根据Key获取参数值
	 * 
	 * @param name
	 * @return
	 */
	public String getProperty(String name) {
		return propertiesMap.get(name);
	}

}
