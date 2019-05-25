package org.xxjr.store.util;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpsClinetTrustAnyUtil {

	
	private static HttpsClinetTrustAnyUtil instance = null;
	private HttpsClinetTrustAnyUtil(){}
	public static HttpsClinetTrustAnyUtil getInstance(){
		if (instance == null) {
			synchronized (HttpsClinetTrustAnyUtil.class) {
				if (instance == null) {
					instance = new HttpsClinetTrustAnyUtil();
				}
			}
		}
		return instance;
	}
	
	
	public  CloseableHttpClient getHttpsClient() throws Exception{
		return HttpClients.custom()
	    		.setSSLHostnameVerifier(hv)
	    		.setSSLContext(trustAllHttpsCertificates())
	    		.build();  

	}
	
	 HostnameVerifier hv = new HostnameVerifier() {  
        public boolean verify(String urlHostName, SSLSession session) {  
            return true;  
        }  
    };  

      
     SSLContext trustAllHttpsCertificates() throws Exception {  
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext  
                .getInstance("SSL","SunJSSE");
        sc.init(null, new TrustManager[] {new TrustAnyTrustManager()}, new java.security.SecureRandom());
        return sc;
    }
     
     private static class TrustAnyTrustManager extends X509ExtendedTrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain,
				String authType, Socket socket) throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain,
				String authType, SSLEngine engine) throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain,
				String authType, Socket socket) throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain,
				String authType, SSLEngine engine) throws CertificateException {
			// TODO Auto-generated method stub
			
		}
    	 
     }
    
}
