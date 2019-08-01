/*
 * 反射接口类
 *
 */
package org.llw.com.core.service;

import org.llw.com.context.AppParam;
import org.llw.com.context.AppResult;

public abstract interface SoaInvoker {
	public abstract AppResult invoke(AppParam context);
	public abstract AppResult busInvoke(AppParam context);
}