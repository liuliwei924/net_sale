package org.ddq.common.core.service;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.springframework.stereotype.Component;


@Component
public class RemoteInvoke {
	
	public static RemoteInvoke getInstance(){
		return SpringAppContext.getBean(RemoteInvoke.class);
	}
	
	
	/***
	 * 调用 事务处理
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public AppResult call(final AppParam params) {
		//return invoke(params, TxType.DEF_TX);
		params.setDataBase(params.getRmiServiceName() + "_");
		return SoaManager.getInstance().invoke(params);
	}

	/***
	 * 不调用事务处理
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public AppResult callNoTx(final AppParam params) {
		//return invoke(params, TxType.NO_TX);
		params.setDataBase(params.getRmiServiceName() + "_");
		return SoaManager.getInstance().callNoTx(params);
	}
}
