/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package org.ddq.common.context;

import java.io.FileNotFoundException;

import org.ddq.common.core.SpringAppContext;
import org.ddq.common.util.LogerUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * spring中获取资源文件
 * 
 * @author liulw
 * 
 */
public class AppProperties {
	
	public static Environment environment;

	public static Environment getEnvironment() {
		if (environment == null) {
			environment = initEnvironment();
		}
		return environment;
	}
	
	public static Environment initEnvironment() {
		ApplicationContext applicationContext = SpringAppContext
				.getApplicationContext();
		Environment environment = applicationContext.getEnvironment();
		return environment;
	}
	
	public static String getProperties(String name) {
		return getEnvironment().getProperty(name);
	}
	
	/**
	 * 获取真实的路径，不包含jar 包的路径
	 * @param type 
	 * @return the file UploadPath
	 */
	public static String getRealPath() {
		String path =null;
		try {
			String oldPath = ResourceUtils.getURL("classpath:").getPath();
			if(oldPath.indexOf("jar!")>0) {
				path = oldPath.substring(0, oldPath.indexOf("jar!"));
				path = path.substring("file:".length(),path.lastIndexOf("/"));
			}else {
				path = oldPath;
			}
			LogerUtil.debug("RealPath:" +oldPath +" new:" + path );
		} catch (FileNotFoundException e) {
			LogerUtil.error(AppProperties.class,e, "getRealPath error:");
		}
		return path;
	}
	
	/***
	 * 系统是否debug状态
	 * @return
	 */
	public static boolean isDebug(){
		String debug =  getEnvironment().getProperty(DEBUG_FLAG);
		if(StringUtils.isEmpty(debug)){
			return false;
		}
		return Boolean.valueOf(debug);
	}
	
	public static void main(String[] args) {
		String oldPath = "file:/F:/mall/new-xxjr/webother/xxjr-page-web/target/xxjr-page-web-3.5.0.jar!/BOOT-INF/classes!/";
		String path = oldPath.substring(0, oldPath.indexOf("jar!"));
		path = path.substring("file:/".length(),path.lastIndexOf("/"));
		System.out.println(path);
	}
	
	/**debug状态*/
	public final static String DEBUG_FLAG ="system.debug";
	
	/**下载路径 */
	public final static String KeyDownloadTempPath = "download.path";
	

	/**密码是否添加加密串*/
	public final static String PASSWORD_IS_ENCRYPT = "password.encrypt";
	/**加密处理多加串*/
	public final static String PASSWORD_MD5_KEY = "password.encrypt.key";
	
	
	
}