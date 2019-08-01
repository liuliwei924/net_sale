package org.llw.com.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;


public class JsonUtil{

	private JsonUtil() {
	}
	private static class InnerClassSingleton {
		private final static JsonUtil jsonUtil = new JsonUtil();
	 }
	
	public static JsonUtil getInstance(){
		return InnerClassSingleton.jsonUtil;
	}

	public String object2JSON(Object obj,SerializerFeature... serializerFeature) {
		if(obj == null){
			return "{}";
		}
		return JSON.toJSONString(obj,serializerFeature);
	}
	
	public String object2JSON(Object obj) {
		if(obj == null){
			return "{}";
		}
		return JSON.toJSONString(obj,SerializerFeature.WriteDateUseDateFormat);
	}
	

	public <T>  T json2Object(String json,Class<T> clazz) {
		if(json == null || json.isEmpty()){
			return null;
		}
		return JSON.parseObject(json, clazz);
	}
	
	public <T> T json2Reference(String json, TypeReference<T> reference){
		if(json == null || json.isEmpty()){
			return null;
		}
		return JSON.parseObject(json, reference);
	}
	
	
	/***
	 * 
	 * @param array
	 * @param className
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>>  getListFromJsonArray(JSONArray array) {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = (JSONObject) array.get(i);
			Map<String,Object> obj = new LinkedHashMap<String,Object>();
			for (Object key : jsonObject.keySet()) {
				String keyName = key.toString();
				Object value = jsonObject.get(key);
				obj.put(keyName, value);
			}
			list.add(obj);
		}
		return list;
	}
}