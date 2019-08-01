package org.llw.com.exception;

import org.llw.com.constant.DuoduoConstant;
import org.llw.com.context.AppResult;

public class DuoduoError {
	/**500以上*/
	
	/**--------------------用户相关 500-999---------------------------------**/
	/**未登录系统**/
	public static final int LOGIN_ERROR_1=501;
	/**已经在其他地方系统登录**/
	public static final int LOGIN_ERROR_2=502;
	/**用户状态不正确**/
	public static final int LOGIN_ERROR_3=503;
	/**用户名或密码不正确**/
	public static final int LOGIN_ERROR_4=504;
	/**验证码不正确**/
	public static final int LOGIN_ERROR_5=505;
	/**用户添加错误，用户名已经存在**/
	public static final int USER_ERROR_6=506;
	/**不可以删除admin用户*/
	public static final int USER_DELETE_ADMIN = 507;
	/**修改密码时，原密码不正确*/
	public static final int USER_EDIT_PWD_OLD_IS_ERROR = 508;
	/**用户修改时，用户信息不存在*/
	public static final int USER_EDIT_USER_IS_NOT_EXISTS = 509;
	/**修改密码时，两次密码不正确*/
	public static final int USER_EDIT_PWD_IS_NOT_EQUALS = 510;
	
	
	/**-------------------系统相权限或操作相关 code 1000---------------------------------**/
	/**请求系统时，相应的URL没有权限*/
	public static final int REQUEST_ERROR_NO_URL = 1001;
	/**请求系统时，相应的服务名下的方法 没有权限*/
	public static final int REQUEST_ERROR_NO_SERVICE_METHOD = 1002;
	
	/**form处理时，相应的form不存在*/
	public static final int FORM_IS_NOT_EXISTS = 1003;
	/**table处理时，相应的table不存在*/
	public static final int TABLE_IS_NOT_EXISTS = 1004;
	
	/**数据操作时的处理*/
	/**数据保存失败*/
	public static final int DATE_SAVE_ERROR = 1005;
	/** 删除时没有添加任何参数*/
	public static final int DELETE_NO_ID = 1006;
	/** 数据修改时，没有添加任何参数*/
	public static final int UPDATE_PARAM_NO_FIEDS = 1007;
	/**数据修改时，参数缺少*/
	public static final int UPDATE_NO_PARAMS = 1008;
	/**数据修改时，数据已经不存在*/
	public static final int UPDATE_DATA_IS_NOTEXISTS = 1009;
	/**数据修改时，系统内置数据不允许修改*/
	public static final int UPDATE_DATA_IS_DEFAULT = 1010;
	
	/**角色删除时，存在其他用户 */
	public static final int ROLE_DELETE_HAVE_USER = 1011;
	/**角色添加时，没有设置 菜单信息*/
	public static final int ROLE_ADD_HAVE_NO_MENU = 1012;
	/**角色添加时，没有设置 权限*/
	public static final int ROLE_ADD_HAVE_NO_RIGHT = 1013;
	/**权限添加时，没有设置方法*/
	public static final int RIGHT_ADD_HAVE_NO_METHOD = 1014;

	/**删除用户组时，存在用户*/
	public static final int USER_GROUP_HAVE_USER = 1015;
	
	
	/**--------------------系统相关业务 start 2000---------------------------------**/
	
	
	/***
	 * 验证数据是否修改，若已经修改，则报错
	 * @param result
	 */
	public static void validUpdateResult(AppResult result){
		if(Integer.valueOf(result.getAttr(DuoduoConstant.DAO_Update_SIZE).toString()) !=1){
			throw new AppException(DuoduoError.UPDATE_DATA_IS_NOTEXISTS);
		}
	}
}

