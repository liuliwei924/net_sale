package org.xxjr.busi.util.kf;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;
import org.xxjr.cust.util.CustConstant;
import org.xxjr.sys.util.AreaUtils;
import org.xxjr.sys.util.NumberUtil;


/**
 * 贷上我落地页申请工具类
 * @author Administrator
 *
 */
public class D35LoanApplyUtil {

	public static final List<String> channels = new ArrayList<String>();
	
	/**聚合appkey  1、手机固话来电显示  2、手机归属地查询**/
	private static final String[] appKeys = new String[] {"928091df185dc771f831ac1bcba8dda5", "461cf465d187dd0eb6df7ba792a17276"};

	static{
		channels.add("liebao");
		channels.add("fengh");
		channels.add("d35op");
		channels.add("tuia");
		channels.add("xinlang");
		channels.add("ocpc");
		channels.add("baidu");
	}
	
	public static String getChannelDetail (String referer) {
		if (!StringUtils.isEmpty(referer)) {
			for (String key : channels) {
				if (referer.indexOf(key) != -1) {
					return key;
				}
			}
		}
		return CustConstant.CUST_sourceType_WEB;
	}
	
	public static String getRequestRealIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0];
		}

		if (!checkIp(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (!checkIp(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (!checkIp(ip)) {
			ip = request.getHeader("X-Real-IP");
		}

		if (!checkIp(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	private static boolean checkIp(String ip) {
		if (ip == null || ip.length() == 0 || "unkown".equalsIgnoreCase(ip)) {
			return false;
		}
		return true;
	}
	
	 //接口备用地址
    private final static String interfaceAddress = "http://ip.taobao.com/service/getIpInfo.php?ip=";
	
    private static String getIpCity(String ip,String interfaceAddress){
    	InputStream is = null;
    	BufferedReader br = null;
    	StringBuffer buf = new StringBuffer();
    	String city = null;
    	try {
    		HttpClient client = HttpClients.createDefault();
    		
        	HttpGet get = new HttpGet(interfaceAddress+ip);
        	RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
        			                         .setConnectionRequestTimeout(5000).build();
        	get.setConfig(requestConfig);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			if(is==null){
				return null;
			}
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                buf.append(line);
            }
            if(!StringUtils.isEmpty(buf.toString())){
            	String s = buf.toString();
            	String key ="\"city\":" ;
            	int index = s.indexOf(key);
            	if(index!=-1){
	            	int end = s.indexOf("\"",index+key.length()+1);
	            	city = s.substring(index+key.length()+1, end);
	            	city = unicodeToChinese(city);
            	}
            	if(!StringUtils.isEmpty(city) && city.endsWith("市")){
            		city = city.substring(0,city.length()-1);
            	}
            }
		} catch (Exception e) {
			LogerUtil.error(D35LoanApplyUtil.class,e,"get ip city error");
		} finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					
				}
			}
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					
				}
			}
		}
    	
    	return city;
    }
    
    
    /**
     * unicode转中文
     * @param utfString
     * @return
     */
    public static String unicodeToChinese(String utfString){  
        StringBuilder sb = new StringBuilder();  
        int i = -1;  
        int pos = 0;  
          
        while((i=utfString.indexOf("\\u", pos)) != -1){  
            sb.append(utfString.substring(pos, i));  
            if(i+5 < utfString.length()){  
                pos = i+6;  
                sb.append((char)Integer.parseInt(utfString.substring(i+2, i+6), 16));  
            }  
        }  
          
        return sb.toString();  	
    }
    
    
    private final static String getIpAddress     = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=js&ip=";
    /**
     * 获取ip的城市
     * @param ip
     * @return
     */
    public static String getIpCityAndPhone(String ip,String phone){
    	String city = getIpCity(ip, interfaceAddress);
    	if(StringUtils.isEmpty(city)){
    		city = getIpCity(ip, getIpAddress);
    	}
    	if(StringUtils.isEmpty(city)){
    		city = getIpCityByBaidu(ip);
    	}
    	if(StringUtils.isEmpty(city)){
    		city = getRequestCity2(phone);
    	}
    	return city;
    }
    
    private static String getIpCityByBaidu(String ip){
    	String baiduAddress = "http://api.map.baidu.com/location/ip?ak=r6PsaZcHjicgyUDexkG44pRM16fn3wMe&coor=bd09ll&ip=";
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		String responseContent = null;
    	String city = null;
    	try {
    		client = HttpClients.createDefault();
        	HttpGet get = new HttpGet(baiduAddress+ip);
        	RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
        			                         .setConnectionRequestTimeout(5000).build();
        	get.setConfig(requestConfig);
			response = client.execute(get);
			responseContent = URLDecoder.decode(EntityUtils.toString(response.getEntity(), Consts.UTF_8),"UTF-8");
			if(StringUtils.hasText(responseContent)){
				String key ="\"city\":" ;
            	int index = responseContent.indexOf(key);
            	if(index!=-1){
	            	int end = responseContent.indexOf("\"",index+key.length()+1);
	            	city = responseContent.substring(index+key.length()+1, end);
	            	city = unicodeToChinese(city);
            	}
            	if(!StringUtils.isEmpty(city) && city.endsWith("市")){
            		city = city.substring(0,city.length()-1);
            	}
			}
			//log.info("百度定位信息：" + unicodeToChinese(responseContent));
		} catch (Exception e) {
			//log.error("get ip city error"+ip,e);
		} finally{
			try {
				// 关闭连接,释放资源
				if (response != null) {
					response.close();
				}
				if (client != null) {
					client.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
    	return city;
    }
    
    //手机归属地查询
    @SuppressWarnings("rawtypes")
	public static String getRequestCity2(String phone){
    	if(!matchPhone(phone)){
    		return null;
    	}
        String result =null;
        String url ="http://apis.juhe.cn/mobile/get";//请求接口地址
        Map<String, String> params = new HashMap<String, String>();//请求参数
        params.put("phone",phone);//需要查询的手机号码或手机号码前7位
        params.put("key",appKeys[1]);//应用APPKEY(应用详细页查询)
        params.put("dtype","json");//返回数据的格式,xml或json，默认json
        try {
            result =net(url, params, "GET");
            Map object = JsonUtil.getInstance().json2Object(result, Map.class);
            if((NumberUtil.getInt(object.get("error_code"), 1))==0){
            	if(object.get("result")!=null){
            		Map data = (Map)object.get("result");
            		String city = null;
            		if (StringUtils.isEmpty(data.get("city"))) {
            			city = StringUtil.getString(data.get("province"));
					}else {
						city = StringUtil.getString(data.get("city"));
					}
                	city = AreaUtils.getLikeCityName(city);;
            		return city;
            	}
            }else{
            	LogerUtil.log(D35LoanApplyUtil.class, "getRequestCity2 error phone:"+ phone +" errorCode:" + object.get("error_code"));
                return null;
            }
        } catch (Exception e) {
        	LogerUtil.error(D35LoanApplyUtil.class, e, "getRequestCity2 error phone:" + phone);
        }
        return null;
    }
    
    //手机固话来电显示
    @SuppressWarnings("rawtypes")
	public static Map<String, Object> getRequestCity3(String phone){
    	if(!matchPhone(phone)){
    		return null;
    	}
    	Map<String, Object> resMap = new HashMap<String, Object>();
    	
        String result =null;
        String url ="http://op.juhe.cn/onebox/phone/query";//请求接口地址
        Map<String, String> params = new HashMap<String, String>();//请求参数
        params.put("tel",phone);//需要查询的手机号码或手机号码前7位
        params.put("key",appKeys[0]);//应用APPKEY(应用详细页查询)
        params.put("dtype","json");//返回数据的格式,xml或json，默认json
        try {
            result =net(url, params, "GET");
            Map object = JsonUtil.getInstance().json2Object(result, Map.class);
            int error_code = NumberUtil.getInt(object.get("error_code"), 1);
            if(error_code==0){
            	if(object.get("result")!=null){
            		Map data = (Map)object.get("result");
            		String city = null;
            		if (StringUtils.isEmpty(data.get("city"))) {
            			city = StringUtil.getString(data.get("province"));
					}else {
						city = StringUtil.getString(data.get("city"));
					}
                	city = AreaUtils.getLikeCityName(city);
                	resMap.put("cityName", city);
                	resMap.put("iszhapian", data.get("iszhapian"));
            	}
            }else if (error_code == 10012) {
				resMap.put("cityName", getRequestCity2(phone));
			}else{
            	LogerUtil.log(D35LoanApplyUtil.class, "getRequestCity3 error phone:"+ phone +" errorCode:" + object.get("error_code"));
            }
        } catch (Exception e) {
        	LogerUtil.error(D35LoanApplyUtil.class, e, "getRequestCity3 error phone:" + phone);
        }
        return resMap;
    }
    
    
    public static boolean matchPhone(String s){
		return Pattern.matches("^1\\d{10}$", s);
	}
    
    /**
    *
    * @param strUrl 请求地址
    * @param params 请求参数
    * @param method 请求方法
    * @return  网络请求字符串
    * @throws Exception
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String net(String strUrl, Map params,String method) throws Exception {
       HttpURLConnection conn = null;
       BufferedReader reader = null;
       String rs = null;
       try {
           StringBuffer sb = new StringBuffer();
           if(method==null || method.equals("GET")){
               strUrl = strUrl+"?"+urlencode(params);
           }
           URL url = new URL(strUrl);
           conn = (HttpURLConnection) url.openConnection();
           if(method==null || method.equals("GET")){
               conn.setRequestMethod("GET");
           }else{
               conn.setRequestMethod("POST");
               conn.setDoOutput(true);
           }
           conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
           conn.setUseCaches(false);
           conn.setConnectTimeout(30000);
           conn.setReadTimeout(30000);
           conn.setInstanceFollowRedirects(false);
           conn.connect();
           if (params!= null && method.equals("POST")) {
               try {
                   DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                   out.writeBytes(urlencode(params));
               } catch (Exception e) {
                   e.printStackTrace();
               }
                
           }
           InputStream is = conn.getInputStream();
           reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
           String strRead = null;
           while ((strRead = reader.readLine()) != null) {
               sb.append(strRead);
           }
           rs = sb.toString();
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           if (reader != null) {
               reader.close();
           }
           if (conn != null) {
               conn.disconnect();
           }
       }
       return rs;
   }
   
   //将map型转为请求参数型
   @SuppressWarnings("rawtypes")
	public static String urlencode(Map<String,String> data) {
       StringBuilder sb = new StringBuilder();
       for (Map.Entry i : data.entrySet()) {
           try {
               sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
           } catch (UnsupportedEncodingException e) {
               e.printStackTrace();
           }
       }
       return sb.toString();
   }
}
