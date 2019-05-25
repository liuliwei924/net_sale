package org.xxjr.sys.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.SpringAppContext;
import org.ddq.common.core.service.RemoteInvoke;
import org.ddq.common.core.service.SoaManager;
import org.llw.model.cache.RedisUtils;


/***
 * 文件类型配置
 * @author qinxcb
 *
 */
public class FileGroupUtil {
	public final static  String KEY_CONFIG = "FileGroup";
	/**上传绝对路径 */
	public final static String KEY_PP_PATH="upload.path";
	/**上传浏览路径 */
	public final static String KEY_PP_PATH_LOCAL="upload.path.local";
	/** 腾讯云CDN路径 */
	public final static String Static_File_Host = "https://static.xxjr.com";
	
	/**
	 * 获取文件类型
	 * @return
	 */
	public static String getFileTypes(String fileType){
		Map<String,Object> fileGroup = getFileGroup(fileType);
		if (fileGroup == null) {
			return "";
		}
		return fileGroup.get("typeAll").toString();
	}
	
	
	
	/**
	 * 获取文件组配置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getFileGroup(String groupCode){
		List<Map<String,Object>> fileGroups = (List<Map<String, Object>>) RedisUtils.getRedisService().get(KEY_CONFIG);
		if(fileGroups ==null || fileGroups.size()==0 || AppProperties.isDebug()){
			fileGroups = refershConfig();
		}
		for(Map<String,Object> fileGroup : fileGroups){
			if(fileGroup.get("groupCode").equals(groupCode)){
				return fileGroup;
			}
		}
		return null;
	}
	
	
	/**
	 * 刷新缓存
	 * @return
	 */
	public static List<Map<String,Object>> refershConfig(){
		AppParam param = new AppParam();
		param.setService("fileGroupService");
		param.setMethod("query");
		AppResult queryResult  = null;
		//若没有相应的对象，使用远程调用 
		if (SpringAppContext.getBean("fileGroupService") == null) {
			queryResult = RemoteInvoke.getInstance().call(param);
		}else{
			queryResult = SoaManager.getInstance().invoke(param);
		}
		List<Map<String,Object>> rows =  queryResult.getRows();
			//刷新摇奖配置
		RedisUtils.getRedisService().set(KEY_CONFIG, (Serializable)rows);
		return rows;
	}
	
	
	/**
	 * 本地文件上传
	 * @return
	 */
	public static AppResult uploadLocalFile(String fileType,String fileName,String oldName){
		//保存上传信息
		AppParam param = new AppParam();
		param.setService("fileListService");
		param.setMethod("insert");
		param.addAttr("fileId", fileName);
		param.addAttr("groupCode", fileType);
		param.addAttr("oldName", oldName);
		return RemoteInvoke.getInstance().call(param);
	}
}
