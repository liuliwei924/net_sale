package org.llw.com.http;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.llw.com.core.SpringAppContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author 2017-06-27 by liulw
 *
 */
@Slf4j
public class HttpClientUtil {

	private CloseableHttpClient httpClient = (CloseableHttpClient)SpringAppContext.getBean("closeableHttpClient");
	
	private CloseableHttpClient httpsClientIgnore = (CloseableHttpClient)SpringAppContext.getBean("closeableHttpClientIgnore");
	
	private HttpClientUtil(){}
	
	private static class InnerClassSingleton {
		private final static HttpClientUtil instance = new HttpClientUtil();
	 }
	
	public static HttpClientUtil getInstance(){
		return InnerClassSingleton.instance;
	}
	
	/**
	 * 发送 post请求
	 * @param httpUrl 地址
	 */
	public String sendHttpPost(String httpUrl) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost  
		return sendHttpPost(httpPost);
	}
	
	/**
	 * 发送 post请求
	 * @param httpUrl 地址
	 * @param params 参数(格式:key1=value1&key2=value2)
	 */
	public String sendHttpPost(String httpUrl, String params) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost  
		try {
			//设置参数
			StringEntity stringEntity = new StringEntity(params, "UTF-8");
			stringEntity.setContentType("application/x-www-form-urlencoded");
			httpPost.setEntity(stringEntity);
		} catch (Exception e) {
			log.error(" http post", e);
		}
		return sendHttpPost(httpPost);
	}
	
	/**
	 * 发送 post请求 忽略https
	 * @param httpUrl 地址
	 * @param params 参数(格式:key1=value1&key2=value2)
	 */
	public String sendHttpPostIgnore(String httpUrl, String params) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost  
		try {
			//设置参数
			StringEntity stringEntity = new StringEntity(params, "UTF-8");
			stringEntity.setContentType("application/x-www-form-urlencoded");
			httpPost.setEntity(stringEntity);
		} catch (Exception e) {
			log.error(" http post", e);
		}
		return sendHttpPostByIgnoreHttps(httpPost);
	}
	
	/**
	 * 发送 post请求
	 * @param httpUrl 地址
	 * @param maps 参数
	 */
	public String sendHttpPost(String httpUrl, Map<String, ?> maps) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost  
		// 创建参数队列  
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String key : maps.keySet()) {
			if(maps.get(key)!=null){
				nameValuePairs.add(new BasicNameValuePair(key, maps.get(key).toString()));
			}
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		} catch (Exception e) {
			log.error("request:" + httpUrl, e);
		}
		return sendHttpPost(httpPost);
	}
	
	/**
	 * 发送 post请求
	 * @param httpUrl 地址
	 * @param maps 参数
	 */
	public String sendHttpPostIgnore(String httpUrl, Map<String, ?> maps) {
		HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost  
		// 创建参数队列  
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String key : maps.keySet()) {
			if(maps.get(key)!=null){
				nameValuePairs.add(new BasicNameValuePair(key, maps.get(key).toString()));
			}
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		} catch (Exception e) {
			log.error("request:" + httpUrl, e);
		}
		return sendHttpPostByIgnoreHttps(httpPost);
	}
	
	/**
	 * 发送Post请求
	 * @param httpPost
	 * @return
	 */
	private String sendHttpPost(HttpPost httpPost) {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String responseContent = null;
		try {
			// 创建默认的httpClient实例.
			// 执行请求
			response = httpClient.execute(httpPost);
			entity = response.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			log.error(" send http post:" , e);
		} finally {
			try {
				if(response != null) response.close();
				
			}catch (Exception e) {
				log.error("httpClient关闭流异常",e);
			}
		}
		return responseContent;
	}
	
	/**
	 * 发送Post请求
	 * @param httpPost
	 * @return
	 */
	private String sendHttpPostByIgnoreHttps(HttpPost httpPost) {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String responseContent = null;
		try {
			
			// 执行请求
			response = httpsClientIgnore.execute(httpPost);
			entity = response.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			log.error(" send http post:" , e);
		} finally {
			try {
				if(response != null) response.close();
			
			}catch (Exception e) {
				log.error("httpClient关闭流异常",e);
			}
		}
		return responseContent;
	}

	/**
	 * 发送 get请求
	 * @param httpUrl 请求地址
	 * @param heads head参数
	 */
	public String sendHttpGet(String httpUrl,Map<String, ?> heads) {
		HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
		for (String key : heads.keySet()) {
			if(heads.get(key)!=null){
				httpGet.addHeader(key, heads.get(key).toString());
			}
		}
		return sendHttpGet(httpGet);
	}
	
	/**
	 * 发送 get请求
	 * @param httpUrl
	 */
	public String sendHttpGet(String httpUrl) {
		HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
		return sendHttpGet(httpGet);
	}
	
	/**
	 * 发送 get请求
	 * @param httpUrl
	 */
	public String sendHttpGetIgnore(String httpUrl) {
		HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
		return sendHttpsGetIgnore(httpGet);
	}
	
	/**
	 * 发送 get请求Https
	 * @param httpUrl
	 */
	public String sendHttpsGet(String httpUrl) {
		HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
		return sendHttpsGet(httpGet);
	}
	
	/**
	 * 发送Get请求
	 * @param httpPost
	 * @return
	 */
	private String sendHttpGet(HttpGet httpGet) {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String responseContent = null;
		try {
			// 执行请求
			response = httpClient.execute(httpGet);
			entity = response.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(response != null) response.close();
			
			}catch (Exception e) {
				log.error("httpClient关闭流异常",e);
			}
		}
		return responseContent;
	}
	
	/**
	 * 发送Get请求
	 * @param httpPost
	 * @return
	 */
	private String sendHttpsGetIgnore(HttpGet httpGet) {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String responseContent = null;
		try {

			// 执行请求
			response = httpsClientIgnore.execute(httpGet);
			entity = response.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(response != null) response.close();
			
			}catch (Exception e) {
				log.error("httpClient关闭流异常",e);
			}
		}
		return responseContent;
	}
	
	/**
	 * 发送Get请求Https
	 * @param httpPost
	 * @return
	 */
	private String sendHttpsGet(HttpGet httpGet) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		String responseContent = null;
		try {
			// 创建默认的httpClient实例.
			PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.load(new URL(httpGet.getURI().toString()));
			DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(publicSuffixMatcher);
			httpClient = HttpClients.custom().setSSLHostnameVerifier(hostnameVerifier).build();
			
			 RequestConfig requestConfig = RequestConfig.custom()
		                .setConnectTimeout(10000)//设置连接超时时间，默认1秒
		                .setSocketTimeout(30000).build();//设置读超时时间，默认10秒
			 
			httpGet.setConfig(requestConfig);
			// 执行请求
			response = httpClient.execute(httpGet);
			entity = response.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			log.error(" http get:", e);
		} finally {
			try {
				if(response != null) response.close();
				if(httpClient != null) httpClient.close();
			
			}catch (Exception e) {
				log.error("httpClient关闭流异常",e);
			}
		}
		return responseContent;
	}
}
