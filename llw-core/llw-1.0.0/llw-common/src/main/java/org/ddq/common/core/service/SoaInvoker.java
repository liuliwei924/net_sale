/*
 * 反射接口类
 *
 */
package org.ddq.common.core.service;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;

public abstract interface SoaInvoker {
	public abstract AppResult invoke(AppParam context);
	public abstract AppResult busInvoke(AppParam context);
}