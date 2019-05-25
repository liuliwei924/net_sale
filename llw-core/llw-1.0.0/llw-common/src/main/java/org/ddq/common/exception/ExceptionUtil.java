package org.ddq.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.ddq.common.constant.DuoduoConstant;
import org.ddq.common.context.*;


public class ExceptionUtil {

	/***
	 * 获取给用户看到的错误信息
	 * @param ex 抛出的错误
	 * @param mapReturn 返回的map对象
	 * @return
	 */
	public static void setExceptionMessage(Throwable ex,AppResult context,AppParam logContext) {
		String simpleName = ex.getClass().getSimpleName();
		context.setSuccess(false);
		setLogDesciption(logContext, ex);
		if(ex instanceof AppException ){
			AppException myException = (AppException) ex;
			int errorCode = myException.getErrorCode();
			context.setErrorCode(errorCode+"");
			context.setMessage("处理失败:" + ex.getMessage());
		} else if (ex instanceof  SysException) {
			SysException myException = (SysException) ex;
			int errorCode = myException.getErrorCode();
			context.setErrorCode(errorCode+"");
			context.setMessage("处理失败:" + ex.getMessage());
		}else if("QuerySyntaxException".equals(simpleName)||
				"CannotCreateTransactionException".equals(simpleName)
				||"CommunicationsException".equals(simpleName)){
			context.setErrorCode(ErrorCode.ERROR_DATABASE_ACCESS_100+"");
			context.setMessage("ERROR_DATABASE_ACCESS_100 " + ErrorCode.ERROR_DATABASE_ACCESS_100);
		} else {
			context.setErrorCode(ErrorCode.DEFAULT_ERROR+"");
			context.setMessage("处理失败:" + ex.getMessage());
		}
	}

	/***
	 * 将throwable 的描述到log
	 * 
	 * @param logContext
	 * @param ex
	 */
	public static void setLogDesciption(AppParam logContext, Throwable ex) {
		if (logContext == null) {
			return;
		}
		String descriptoin = (String) logContext.getAttr(DuoduoConstant.LogDescription);
		if (descriptoin == null) {
			logContext.addAttr(DuoduoConstant.LogDescription, ex.getMessage());
		} else {
			logContext.addAttr(DuoduoConstant.LogDescription, descriptoin
					+ " error:" + ex.getMessage());
		}
	}

	/***
	 * 获取exception的详细信息
	 * 
	 * @param exs
	 * @return
	 */
	public static String getExceptionDetail(Throwable ex) {
		try {
			return exceptionFile(ex);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	/***
	 * 错误文件的处理
	 * @param ex
	 * @param type 1返回错误的详细信息，文件删除, 其他时候返回生成的文件名
	 * @return
	 * @throws Exception
	 */
	private static String exceptionFile(Throwable ex)
			throws Exception {
		StringWriter str = new StringWriter();
		PrintWriter out = new PrintWriter(str);
		ex.printStackTrace(out);
		out.close();
		return str.toString();
	}
}
