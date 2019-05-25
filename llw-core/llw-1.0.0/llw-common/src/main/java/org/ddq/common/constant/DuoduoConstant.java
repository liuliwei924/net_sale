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
package org.ddq.common.constant;



public class DuoduoConstant {
	/**用户类型 后台管理员**/
	public static final int USER_TYPE_2_ADMIN = 2; //
	/**用户类型 前端客户**/
	public static final int USER_TYPE_1_CUSTOMER = 1; //
	
	
	/**用户状态 正常**/
	public static final int USER_STATUS_1 = 1; //
	/**用户状态  被禁用**/
	public static final int USER_STATUS_2 = 2; //
	/**用户状态  被删除**/
	public static final int USER_STATUS_3 = 3; //
	
	/**该菜单可以编辑**/
	public static final int MENU_IS_EDIT = 1; //
	/**该菜单不可心编辑**/
	public static final int MENU_IS_NOT_EDIT = 1; //
	
	
	/**查询总数返回的KEY**/
	public static final String TOTAL_SIZE = "totalSize";
	

	/**DAO 添加返回的KEY**/
	public static final String DAO_Insert_SIZE = "insertSize";
	/**DAO 修改返回的KEY**/
	public static final String DAO_Update_SIZE = "updateSize";
	/**DAO 删除返回的KEY**/
	public static final String DAO_Delete_SIZE = "deleteSize";
	
	/**排序关键字**/
	public final static String SORT = "sort";
	/**排序关键字--降序**/
	public final static String SortDESC="DESC";
	/**排序关键字--升序**/
	public final static String SortASC="ASC";
	
	/**字段 类型--字符串**/
	public final static String SortPinyin="pinyin";
	/**排序关键字--数字，整数**/
	public final static String SortNumber="number";
	/**排序关键字--数字，按千分隔数字**/
	public final static String SortNumberlike="numberLike";
	/**排序关键字--数字，按流量计算**/
	public final static String SortNumberSize="numberSize";
	/**排序关键字--百分比类型**/
	public final static String SortPercent="percent";
	
	/** 日志的描述**/
	public final static String LogDescription = "description";
	/**RMI 本地IP配置**/
	public final static String LOCAL_IP = "local.ip";
	/**RMI服务的开头配置**/
	public final static String RMI_SERVICE_START="rmi.service.";
	/**远程服务的开头配置**/
	public final static String REMOTE_SERVICE_START="remote.server.";
	
	/**远程服务的任务管理服务**/
	public final static String REMOTE_SERVICE_ADMIN="admin";
}