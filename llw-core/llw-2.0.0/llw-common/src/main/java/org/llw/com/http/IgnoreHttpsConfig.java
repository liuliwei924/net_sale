package org.llw.com.http;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

public class IgnoreHttpsConfig implements HostnameVerifier {

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
	
	public SSLContext trustAllHttpsCertificates() throws Exception {  
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
