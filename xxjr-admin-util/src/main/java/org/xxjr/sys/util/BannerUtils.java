package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.RemoteInvoke;
import org.llw.model.cache.RedisUtils;
import org.springframework.util.StringUtils;

public class BannerUtils {
	/**Banner APP首页*/
	public static final String Banner_Type_1="1";
	/**Banner APP资讯*/
	public static final String Banner_Type_2="2";
	/**Banner App武功秘笈*/
	public static final String Banner_Type_3="3";

	//pc端banner
	public  final static int  BANNER_TYPE_PC = 1;
	//app端banner
	public  final static int  BANNER_TYPE_LOAN = 2;
	public  final static int  BANNER_TYPE_BORROW = 3;
	public  final static int  BANNER_TYPE_BORROW_HOME = 4;
	//h5端banner
	public  final static int  BANNER_TYPE_H5 = 5;
	// 小小金融商城的banner
	public final static int BANNER_TYPE_XXJRSHOP = 6;
	// 房地产的banner
	public final static int BANNER_TYPE_FDCSHOP = 7;
	// 保保助手的banner
	public final static int BANNER_TYPE_BBZSSHOP = 8;
	
	public final static String BANNER_REDIS_KEY = "banner_redis_key_";


	/**手机菜单*/
	public static final String PHONE_HOME_MEMU = "phoneHomeMenu";
	/**banner信息*/
	public static final String PHONE_HOME_BANANER = "homeBananer";


	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getPhomeMenu(){
		List<Map<String, Object>> list = (List<Map<String, Object>>)RedisUtils.getRedisService().get(PHONE_HOME_MEMU);
		if(list == null || list.size() == 0){
			list = refreshPhoneHomeMenu();
		}
		return list;
	}

	public static List<Map<String, Object>> refreshPhoneHomeMenu(){
		AppParam param = new AppParam();
		param.setService("sysPhoneMenuService");
		param.setMethod("query");
		param.addAttr("isUse", "1");
		AppResult result = RemoteInvoke.getInstance().call(param);
		List<Map<String, Object>> list =  result.getRows();
		RedisUtils.getRedisService().set(PHONE_HOME_MEMU, (Serializable)list);
		return list;
	}


	/***
	 * 首页相关的banner
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getBanners(String type){
		List<Map<String, Object>> list = (List<Map<String, Object>>)RedisUtils.getRedisService().get(PHONE_HOME_BANANER);
		if(list == null || list.size() == 0){
			list = refreshHomeImgList();
		}
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> banner:list){
			if(type.equals(banner.get("imageType").toString())){
				retList.add(banner);
			}
		}
		return retList;
	}

	public static List<Map<String, Object>> refreshHomeImgList(){
		AppParam param = new AppParam();
		param.setService("sysHomeService");
		param.setMethod("queryShow");
		param.addAttr("enable", "1");
		AppResult result = RemoteInvoke.getInstance().call(param);
		List<Map<String, Object>> list =  result.getRows();
		RedisUtils.getRedisService().set(PHONE_HOME_BANANER, (Serializable)list);
		return list;
	}


	public static List<Map<String,Object>> getBannersPC(){	
		return getBanners(BANNER_TYPE_PC);
	}

	/***
	 *查询贷款人首页 
	 * @return
	 */
	public static List<Map<String,Object>> getBannersLoan(){
		return getBanners(BANNER_TYPE_LOAN);
	}


	/***
	 *查询借款人借款 
	 * @return
	 */
	public static List<Map<String,Object>> getBannersBorrow(){
		return getBanners(BANNER_TYPE_BORROW);
	}

	/***
	 *查询借款人首页 
	 * @return
	 */
	public static List<Map<String,Object>> getBannersBorrowHome(){
		return getBanners(BANNER_TYPE_BORROW_HOME);
	}

	/**
	 * 获取H5banner
	 * @return
	 */
	public static List<Map<String,Object>> getBannersH5(){
		return getBanners(BANNER_TYPE_H5);
	}


	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getBanners(int type){
		List<Map<String,Object>> list = (List<Map<String,Object>>)RedisUtils.getRedisService().get(BANNER_REDIS_KEY);
		
		List<Map<String,Object>> banList = new ArrayList<Map<String,Object>>();
		
		if(list == null || list.isEmpty()){
			list = refreshBannersAll();
		}
		for(int i =0;i<list.size();i++){
			Map<String,Object> map = list.get(i);
			if(StringUtils.isEmpty(map.get("type"))){
				continue;
			}
			int rtype = Integer.parseInt(map.get("type").toString());
			if(rtype == type){
				banList.add(map); 
			}

		}

		return banList;
	}



	/**
	 * 刷新banner 缓存
	 * @return
	 */
	public static List<Map<String,Object>> refreshBannersAll(){
		AppParam params = new AppParam("bannerService","query");
		params.addAttr("status", 1);
		params.setOrderBy("orderIndex");
		params.setOrderValue("ASC");
		AppResult result = RemoteInvoke.getInstance().callNoTx(params);
		List<Map<String,Object>> list = result.getRows();
		if(list != null && !list.isEmpty())
			RedisUtils.getRedisService().set(BANNER_REDIS_KEY, (Serializable)list);
		return list;
	}


}
