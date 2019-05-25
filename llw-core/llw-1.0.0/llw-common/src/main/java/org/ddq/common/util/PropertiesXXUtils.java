package org.ddq.common.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/***
 * 
 * @author xcb 获取参数的处理
 */

public class PropertiesXXUtils {

	private static Map<String, String> propertiesMap = new HashMap<String, String>();
	/** 默认的工程文件 */
	private static String KEY_application = "application";
	/** 文件后缀名 */
	private static String KEY_properties = ".properties";
	/** 需要加载的其他配置文件名 */
	private static String KEY_otherConfigs = "otherConfigs";

	private volatile static PropertiesXXUtils proUtil;

	private PropertiesXXUtils() {
		Properties props = null;
		String profileActive = null;
		try {
			props = loadPropertiesFile(KEY_application + KEY_properties);
			if (props != null) {
				profileActive = props.getProperty("spring.profiles.active");
			}
			// 加载application参数
			processProperties(props);
			LogerUtil.log("spring.profiles.active:" + profileActive);
		} catch (Exception e) {
			LogerUtil.error(PropertiesXXUtils.class,e,"get profileActive error:");
		}
		// 没有配置文件
		if (profileActive == null) {
			if (props != null) {
				processProperties(props);
			}
		} else {
			Properties devProps = loadPropertiesFile(KEY_application + "-" + profileActive + KEY_properties);
			LogerUtil.log("loan :" + profileActive);
			PropertiesXXUtils.processProperties(devProps);
		}
		String otherConfigs = null;
		try {
			otherConfigs = propertiesMap.get(PropertiesXXUtils.KEY_otherConfigs);
			if (otherConfigs == null)
				return;
			for (String config : otherConfigs.split(",")) {
				try {
					props = loadPropertiesFile(config + KEY_properties);
					PropertiesXXUtils.processProperties(props);
				} catch (Exception e) {
					LogerUtil.error(PropertiesXXUtils.class,e,"loadn Properties error:" + config);
				}
			}
		} catch (Exception e) {
			LogerUtil.error(PropertiesXXUtils.class,e,"get otherConfigs error:");
		}

	}

	public static PropertiesXXUtils getInstance() {
		if (proUtil == null) {
			synchronized (PropertiesXXUtils.class) {
				if (proUtil == null) {
					proUtil = new PropertiesXXUtils();
				}
			}
		}
		return proUtil;
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
				LogerUtil.debug(key + "=" + value);
			} catch (Exception e) {
				LogerUtil.error(PropertiesXXUtils.class,e,"processProperties error:");
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
			LogerUtil.error(PropertiesXXUtils.class,e,"loadn Properties error:" + propertyFileName);
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
			LogerUtil.error(PropertiesXXUtils.class,e,"loadn Properties error:" + fileUrl);
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
