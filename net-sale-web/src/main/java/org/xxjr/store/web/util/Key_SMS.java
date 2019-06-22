package org.xxjr.store.web.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ddq.common.security.md5.Md5;

public class Key_SMS {

	/*** 交单跟进平台快捷登录  **/
	public final static String Key_SMS_STORE_KJ_LOGIN = "StorekjLoginKey";

	/**短信发送次数后缀*/
	public final static String SMS_COUNT_FIX = "_count";
	
	/**图形验证码后缀*/
	public final static String IMG_CODE_FIX = "_imgCode";
	
	public static void main(String[] args) {
		String telephone = "18670787211_";
		
		System.out.println(Md5.getInstance().encrypt(telephone));
		int a = telephone.indexOf("_");
		System.out.println("a==" +a);
		System.out.println(telephone.substring(0,a));
		
		List<String> list1 = new ArrayList<>();
		list1.add("1111");
		list1.add("22222");
		list1.add("333333");
		
		int totalSize = list1.size();
		int everySize = 50;
		int xhCount = 1;
		int xhSize = 0;
		while (xhSize < totalSize && xhCount <= 100) {// 最多循环100次
			int formIndex = (xhCount-1)*everySize ;
			int toIndex =  xhCount*everySize ;
			if(toIndex > totalSize){
				toIndex = totalSize;
			}
			if(formIndex > toIndex){
				formIndex = toIndex;
			}
			List<String> subDataList = new ArrayList<String>(list1.subList(formIndex, toIndex));
			
			System.out.println("subDataList=" + subDataList);
			
			xhSize = xhSize + subDataList.size();
			xhCount ++;
			
			System.out.println("xhSize=" + xhSize);
			System.out.println("xhCount=" + xhCount);
			
		}
	}
}
