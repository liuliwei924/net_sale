package org.llw.com.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetUtil {
	private static String localIp = null;
	/***
	 * 获取当前内网IP
	 * @return
	 */
	public static String getInetAddress() {
		if(!StringUtils.isEmpty(localIp)){
			return localIp;
			
		}
		//再获取相应IP
		if(StringUtils.isEmpty(localIp)){
			localIp = getLocalAddress0().getHostAddress();
		}
		return localIp;
	}

	public static final String LOCALHOST = "127.0.0.1";

	public static final String ANYHOST = "0.0.0.0";

	private static final Pattern IP_PATTERN = Pattern
			.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
	
	private static boolean isValidAddress(InetAddress address) {
		if (address == null || address.isLoopbackAddress())
			return false;
		String name = address.getHostAddress();
		return (name != null && !ANYHOST.equals(name)
				&& !LOCALHOST.equals(name) && IP_PATTERN.matcher(name)
				.matches());
	}

	private static InetAddress getLocalAddress0() {
		InetAddress localAddress = null;
		try {
			localAddress = InetAddress.getLocalHost();
			if (isValidAddress(localAddress)) {
				return localAddress;
			}
		} catch (Exception e) {
			log.warn("Failed to retriving ip address, " + e.getMessage(), e);
		}
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			if (interfaces != null) {
				while (interfaces.hasMoreElements()) {
					try {
						NetworkInterface network = interfaces.nextElement();
						Enumeration<InetAddress> addresses = network
								.getInetAddresses();
						if (addresses != null) {
							while (addresses.hasMoreElements()) {
								try {
									InetAddress address = addresses
											.nextElement();
									if (isValidAddress(address)) {
										return address;
									}
								} catch (Exception e) {
									log.warn(
											"Failed to retriving ip address, "
													+ e.getMessage(), e);
								}
							}
						}
					} catch (Exception e) {
						log.warn(
								"Failed to retriving ip address, "
										+ e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			log.info("Failed to retriving ip address, " + e.getMessage(), e);
		}
		log.error("Could not get local host ip address, will use 127.0.0.1 instead.");
		return localAddress;
	}

}
