package org.llw.com.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectUtils {

	/***
	 * get getMethod
	 * 
	 * @param propderty
	 * @return
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Method getPropertyReadMethod(Class beanClass, String propName) {
		Method method = null;
		try {
			propName = propName.substring(0, 1).toUpperCase()
					+ propName.substring(1, propName.length());
			try {
				method = beanClass.getMethod("get" + propName, new Class[]{});
			} catch (NoSuchMethodException nsex) {
				method = beanClass.getMethod("is" + propName, new Class[]{});
			}
		} catch (NoSuchMethodException ex) {
			log.error("getPropertyReadMethod NoSuchMethodException:" + propName,ex);
			method = null;
		}
		return method;
	}

	/***
	 * invokeMethod
	 * 
	 * @param bean
	 * @param methodName
	 * @param params
	 * @return
	 */
	
	@SuppressWarnings("rawtypes")
	public static Object invokeMethod(Object bean, String methodName,Object params[]) {
		Method method = null;
		try {
			if(params!=null ){
				Class[] paramClass=  new Class[params.length] ;
				for(int i=0;i<params.length;i++){
					paramClass[i] = params[i].getClass();
				}
				method = bean.getClass().getMethod(methodName,paramClass);
			}else{
				Class[] paramClass=  new Class[0] ;
				method = bean.getClass().getMethod(methodName, paramClass);
			}
			return method.invoke(bean, params);
		} catch (NoSuchMethodException ex) {
			log.error(" invokeMethod NoSuchMethodException!" + methodName,ex);
		} catch(Exception e){
			log.error(" invokeMethod error!",e);
		}
		return null;
	}
	
	/***
	 * get setMethod
	 * 
	 * @param propderty
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Method getPropertyWriteMethod(Class beanClass, String propName) {
		try {
			Class[] argTypes = { beanClass.getDeclaredField(propName).getType() };
			propName = propName.substring(0, 1).toUpperCase()
					+ propName.substring(1, propName.length());
			return beanClass.getMethod("set" + propName, argTypes);
		} catch (Exception e) {
			log.error(" invokeMethod getPropertyWriteMethod!" + propName,e);
			return null;
		}
	}

	/***
	 * getClassPropertyValue
	 * 
	 * @param object
	 * @param property
	 * @return
	 * @throws Exception
	 */
	public static Object getPropertyValue(Object object, String property){
		Object obj = object;
		while (property.indexOf(".") > 0) {
			obj = ReflectUtils.getPropertyValue(
					obj,
					property.substring(0,property.indexOf(".")));
			property = property.substring(property.indexOf(".")+1);
		}
		if (obj != null) {
			try {
				return org.apache.commons.beanutils.PropertyUtils.getProperty(obj, property);
			} catch(Exception e){
				return null;
			}
		}
		return null;
	}

	/***
	 * getClassPropertyValue
	 * 
	 * @param object
	 * @param property
	 * @return
	 * @throws Exception
	 */
	public static String getStringPropertyValue(Object object, String property){
		Object value = getPropertyValue(object,property);
		if (value == null) {
			return null;
		}
		if(value.getClass().equals(java.sql.Date.class)) {
			String timeStyle = DateUtil.DATE_PATTERN_YYYY_MM_DD;
			String strValue = DateUtil.toStringByParttern((Date) value,
					timeStyle);
			return strValue;
		}if(isDateTimeObject(value)) {
			String timeStyle = DateUtil.DATE_PATTERN_YYYY_MM_DD_HHMMSS;
			String strValue = DateUtil.toStringByParttern((Date) value,
					timeStyle);
			return strValue;
		}
		return value.toString();
	}
	/***
	 * setClassPropertyValue
	 * 
	 * @param object
	 * @param property
	 * @return
	 * @throws Exception
	 */
	public static void setPropertyValue(Object obj, String property,
			Object value) {
		Field field =null;;
		try {
			field = obj.getClass().getDeclaredField(property);
			Method writeMethod = getPropertyWriteMethod(obj.getClass(), property);
			Object[] args = new Object[] { convert(field.getType(), value) };
			writeMethod.invoke(obj, args);
		} catch(Exception e){
			
		}
	}

	/***
	 * get class Fields name
	 * 
	 * @param className
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<String> getClassFeildsName(Class className) {
		Field[] fields = className.getDeclaredFields();
		List<String> lstField = new ArrayList<String>();
		for (Field field : fields) {
			lstField.add(field.getName());
		}
		return lstField;
	}

	/***
	 * case class
	 * 
	 * @param classToConvert
	 * @param source
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static Object convert(Class classToConvert, Object source)
			throws Exception {
		if (source == null || source.toString().trim().length() == 0) {
			return null;
		}
		if (classToConvert.isInstance(source)) {
			return source;
		}
		if (classToConvert.equals(String.class)) {
			return source.toString();
		}
		if (classToConvert.equals(Integer.class)) {
			return Integer.valueOf(source.toString());
		}
		if (classToConvert.equals(Long.class)) {
			return Long.valueOf(source.toString());
		}
		if (classToConvert.equals(Float.class)) {
			return Float.valueOf(source.toString());
		}
		if (classToConvert.equals(Double.class)) {
			return Double.valueOf(source.toString());
		}
		if (classToConvert.equals(BigDecimal.class)) {
			return new BigDecimal(source.toString());
		}
		if (classToConvert.equals(Date.class)) {
			return toDate(source.toString());
		}
		if (classToConvert.equals(int.class)) {
			return Integer.valueOf(source.toString());
		}
		if (classToConvert.equals(long.class)) {
			return Long.valueOf(source.toString());
		}
		if (classToConvert.equals(double.class)) {
			return Double.valueOf(source.toString());
		}
		if (classToConvert.equals(float.class)) {
			return Float.valueOf(source.toString());
		}
		if (classToConvert.equals(boolean.class)) {
			return new Boolean("true".equals(source.toString())
					|| "1".equals(source.toString()) ? true : false);
		}
		return null;
	}

	private static SimpleDateFormat dateFormat1 = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static SimpleDateFormat dateFormat2 = new SimpleDateFormat(
			"yyyyMMdd");

	public static java.util.Date toDate(String sDate) {
		java.util.Date result = null;
		try {
			if (sDate == null)
				result = null;

			else if (sDate.length() == 10 && sDate.indexOf("-") == 4) {
				result = dateFormat1.parse(sDate);
			} else if (sDate.length() == 8) {
				result = dateFormat2.parse(sDate);
			} else{
				result = java.sql.Date.valueOf(sDate);
			}
		} catch (ParseException e) {
			
		}
		return result;
	}
	
	
	@SuppressWarnings("rawtypes")
	public static boolean isDateTimeObject(Object obj){
		Class classN = obj.getClass();
		if(classN.equals(java.util.Date.class)  ||
				classN.equals(java.sql.Timestamp.class)){
			return true;
		}
		return false;
	}

	/***
	 * test function
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

	}
}
