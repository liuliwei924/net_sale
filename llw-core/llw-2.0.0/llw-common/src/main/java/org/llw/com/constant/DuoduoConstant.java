package org.llw.com.constant;

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
	/**远程调用前缀*/
	public final static String RPC_SERVICE_START = "rpc.service.";

}