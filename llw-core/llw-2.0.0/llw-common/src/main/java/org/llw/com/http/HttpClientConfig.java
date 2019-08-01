package org.llw.com.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "http-client")
@ConditionalOnProperty(name = "http-client.enable",havingValue="true")
public class HttpClientConfig {

	private Integer connectTimeOut = 10000;
	
	private Integer connectionRequestTimeout = 10000;

	private Integer socketTimeOut = 30000;

    private String agent = "agent";

    private Integer maxConnPerRoute = 10;

    private Integer maxTotal = 200;

    public Integer getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(Integer connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public Integer getSocketTimeOut() {
        return socketTimeOut;
    }

    public void setSocketTimeOut(Integer socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public Integer getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
	}
	
    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Integer getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(Integer maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }
  
    /**
    **
    * 首先实例化一个连接池管理器，设置最大连接数、并发连接数
    * @return
    */
   @Bean(name = "httpClientConnectionManager")
   public PoolingHttpClientConnectionManager getHttpClientConnectionManager(){
       PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
       //最大连接数
       httpClientConnectionManager.setMaxTotal(maxTotal);
       //并发数
       httpClientConnectionManager.setDefaultMaxPerRoute(maxConnPerRoute);
       return httpClientConnectionManager;
   }

   /**
    * 实例化连接池，设置连接池管理器。
    * 这里需要以参数形式注入上面实例化的连接池管理器
    * @param httpClientConnectionManager
    * @return
    */
   @Bean(name = "httpClientBuilder")
   public HttpClientBuilder getHttpClientBuilder(@Qualifier("httpClientConnectionManager")PoolingHttpClientConnectionManager httpClientConnectionManager){

       //HttpClientBuilder中的构造方法被protected修饰，所以这里不能直接使用new来实例化一个HttpClientBuilder，可以使用HttpClientBuilder提供的静态方法create()来获取HttpClientBuilder对象
       HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

       httpClientBuilder.setConnectionManager(httpClientConnectionManager);
       httpClientBuilder.setUserAgent(this.agent);
       
       return httpClientBuilder;
   }

   /**
    * 注入连接池，用于获取httpClient
    * @param httpClientBuilder
    * @return
    */
   @Bean("closeableHttpClient")
   public CloseableHttpClient getCloseableHttpClient(@Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder){
       return httpClientBuilder.build();
   }
   
   /**
    * 注入连接池，用于获取httpClient
    * @param httpClientBuilder
    * @return
    */
   @Bean("closeableHttpClientIgnore")
   public CloseableHttpClient getCloseableHttpClientIgnore(@Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder){
	   IgnoreHttpsConfig IgnoreHttpsConfig = new IgnoreHttpsConfig();
	   try {
		return httpClientBuilder	
				   .setSSLHostnameVerifier(IgnoreHttpsConfig)
				   .setSSLContext(IgnoreHttpsConfig.trustAllHttpsCertificates())
				   .build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	   return null;
   }
   

   /**
    * Builder是RequestConfig的一个内部类
    * 通过RequestConfig的custom方法来获取到一个Builder对象
    * 设置builder的连接信息
    * 这里还可以设置proxy，cookieSpec等属性。有需要的话可以在此设置
    * @return
    */
   @Bean(name = "builder")
   public RequestConfig.Builder getBuilder(){
       RequestConfig.Builder builder = RequestConfig.custom();
       return builder.setConnectTimeout(this.getConnectTimeOut())
               .setConnectionRequestTimeout(connectionRequestTimeout)
               .setSocketTimeout(this.socketTimeOut);
   }

   /**
    * 使用builder构建一个RequestConfig对象
    * @param builder
    * @return
    */
   @Bean
   public RequestConfig getRequestConfig(@Qualifier("builder") RequestConfig.Builder builder){
       return builder.build();
   }

}
