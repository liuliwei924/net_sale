package org.xxjr.mq.listener.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解类
 * @author xxjr
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XxjrInitAnnotation {
	 String beanName() default "";
	 
	 String initMethod() default "init";
}
