package org.xxjr.tools.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpUtil {

	public static String post(String url, String param) throws Exception {
        //https调用方式信任所有证书
        //HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).build();
        //http调用方式
        HttpClient httpClient=HttpClients.createDefault();
        HttpPost method = new HttpPost(url);
        StringEntity entity = new StringEntity(param,"utf-8");//解决中文乱码问题    
        entity.setContentEncoding("UTF-8");    
        entity.setContentType("application/json");    
        method.setEntity(entity); 
        HttpResponse response = httpClient.execute(method); 
        // 请求结束，返回结果        
        String result= EntityUtils.toString(response.getEntity());  
        method.releaseConnection();
        return result.replace("\"", "");
        
	}
	
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }
            });
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }
}
