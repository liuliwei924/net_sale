package org.xxjr.busi.util;

import java.util.Map;

import org.xxjr.sys.util.NumberUtil;


public class CountGradeUtil {
	public static String getGrade(Map<String, Object> now) {
		int houseType = NumberUtil.getInt(now.get("houseType"),2);//0-未填写 1-其他房产 2-无房产 3-商品房（有贷款） 4-商品房（无贷款）5-军产房 6-办公楼 8-商铺  7-厂房 9-自建房/小产权房 12-福利房 
		if (judgeHouse(houseType)) {
			return "A";
		}
		int carType = NumberUtil.getInt(now.get("carType"),2);//0-未填写 2-无车 1-其他车 3-贷款车 4-全款车 5-无车、准备购买
		int insurType = NumberUtil.getInt(now.get("insurType"),0);//0 无 1寿险 2 财险
		if (judgeCar(carType)||judgeInsurType(insurType)) {
			return "B";
		}
		int haveWeiLi = NumberUtil.getInt(now.get("haveWeiLi"),-1);//微粒贷额度（-1未填）
		if (haveWeiLi!=-1) {
			return "C";
		}
		int fundType = NumberUtil.getInt(now.get("fundType"),2);//0未知 1 有 2 无
		if (judgeFundType(fundType)) {
			return "D";
		}
		int socialType = NumberUtil.getInt(now.get("socialType"),2);//0未知 1 有 2 无
		int pubAmount = NumberUtil.getInt(now.get("pubAmount"),0);//0无
		int income = NumberUtil.getInt(now.get("income"),0);//0 无
		if (judgeSocialType(socialType)||pubAmount!=0||income!=0) {
			return "E";
		}
		return "F";
	}
	
	/**
	 * 判断房产
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	public static boolean judgeHouse(int haveHouse){
		return (haveHouse >0 && haveHouse !=2);
	}

	
	/**
	 * 判断车产
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	public  static boolean judgeCar(int carType){
		return (carType ==3 || carType == 4);
	}
	
	/**
	 * 判断保单
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	public  static boolean judgeInsurType(int insurType){
		return !(insurType <= 0);
	}
	
	/**
	 * 判断社保
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	public  static boolean judgeSocialType(int socialType){
		return socialType == 1;
	}
	
	/**
	 * 判断公积金
	 * @param haveHouseCfg
	 * @param haveHouse
	 * @return
	 */
	public  static boolean judgeFundType(int fundType){
		return fundType == 1;
	}
	
}
