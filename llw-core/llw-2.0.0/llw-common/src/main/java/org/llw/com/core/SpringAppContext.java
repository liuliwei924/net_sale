package org.llw.com.core;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SpringAppContext implements ApplicationContextAware {

	private static ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		SpringAppContext.context = applicationContext;
		System.out.println("---------------------------------------------------------------------");
		System.out.println("========SpringAppContext init success ========");
        System.out.println("---------------------------------------------------------------------");
	}
	
	public static ApplicationContext getApplicationContext()
			throws BeansException {
		return context;
	}
	
	/***
	 * 判断 beanId 是否存在，存在返回实例，不存在返回null
	 * @param beanId
	 * @return
	 */
	public static Object getBean(String beanId) {
		if (beanId == null || beanId.length() == 0) {
			return null;
		}
		try{
			Object object = context.getBean(beanId);
			return object;
		}catch(Exception e){
		    log.error("not found Bean:" + beanId,e);
		}
		return null;
	}
	
	/***
	 * 判断 class 是否存在，存在返回实例，不存在返回null
	 * @param beanId
	 * @return
	 */
	public static  <T> T getBean(Class<T> clazz) {
		if (clazz == null ) {
			return null;
		}
		try{
			return context.<T>getBean(clazz);
		}catch(Exception e){
			log.error("not found Bean:" + clazz,e);
		}
		return null;
	}
	
	
	
	
	
}