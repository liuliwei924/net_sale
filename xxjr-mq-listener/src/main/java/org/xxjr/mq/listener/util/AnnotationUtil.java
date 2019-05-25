package org.xxjr.mq.listener.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.ddq.common.core.SpringAppContext;
import org.ddq.common.util.LogerUtil;

public class AnnotationUtil {
	
	public static void execXxjrAnnotationInit(Class<? extends Annotation> annotationClass){
		Map<String,Object> beanMaps = SpringAppContext.getApplicationContext().getBeansWithAnnotation(annotationClass);
		LogerUtil.log("**********map:" + beanMaps);
		for(Map.Entry<String, Object> beanMap : beanMaps.entrySet()) {
			LogerUtil.log("key:" + beanMap.getKey() + " value:" + beanMap.getValue());
			Object bean = beanMap.getValue();
			Class<?> beanClass = bean.getClass();
			try {
				Method method = beanClass.getMethod("init");
				method.invoke(bean);
			} catch (Exception e) {
				 LogerUtil.error(AnnotationUtil.class,e, "扫描消费者类注解报错");
			} 
	    }
		
	}

}
