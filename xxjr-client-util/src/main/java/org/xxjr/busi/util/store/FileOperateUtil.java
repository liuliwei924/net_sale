package org.xxjr.busi.util.store;


/**
 * 用于文件操作
 * 
 * @author Administrator
 *
 */
public class FileOperateUtil {

/*	*//**
	 * 上传资料
	 *//*
	public final static String KEY_PP_PATH="upload.path";
	
	*//**
	 * 图片上传
	 * 
	 * @param applyId
	 * @param materialType
	 * @param imgFile
	 * @param suffix
	 * @return
	 * @throws Exception
	 *//*
	public static AppResult imageUpload(String applyId, String materialType,
			String imgFile,String suffix) throws Exception {
	    	AppResult result = new AppResult();
			String realName = getRandomName(applyId, materialType)+suffix;
			AppParam insertParam = new AppParam("applyFileService", "insert");
			insertParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_crmsys));
			insertParam.addAttr("fileName", realName);
			String path=AppProperties.getProperties("imagesUrl");
			path=path.replace("[id]", applyId);
			insertParam.addAttr("materialType", materialType);
			insertParam.addAttr("fileId", "0");
			insertParam.addAttr("path",path);
			byte[] data = Base64.getDecoder().decode(imgFile);
			insertParam.addAttr("applyId", applyId);
			result=RemoteInvoke.getInstance().call(insertParam);
			FTPDealUtil.fileUpload(data, path, realName);
			AppParam queryParam=new AppParam("applyFileService", "query");
			queryParam.setRmiServiceName(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_crmsys);
			queryParam.addAttr("fileId", result.getAttr("fileId"));
			result.putAttr("fileId", result.getAttr("fileId"));
			result.putAttr("resourcesUrl", AppProperties.getProperties("resourcesUrl"));
			result.putAttr("path", path) ;
			result.putAttr("fileName", realName) ;
			return result;
	}
	
	*//**
	 * 视频资料上传
	 * 
	 * @param customerId
	 * @param typeId
	 * @param request
	 * @return
	 * @throws Exception
	 *//*
	public static AppResult videoUpload(String applyId,String context,String suffix) throws Exception {
		AppResult result = new AppResult();
		String path=AppProperties.getProperties("videoUrl");
		path=path.replace("[id]", applyId);
		String realName = getRandomName(applyId, "video")+"."+suffix;
		AppParam insertParam = new AppParam("applyVideoService", "insert");
		insertParam.setRmiServiceName(AppProperties.getProperties(DuoduoConstant.RMI_SERVICE_START+ServiceKey.Key_crmsys));
		insertParam.addAttr("videoId", "0");
		insertParam.addAttr("path",path);
		insertParam.addAttr("suffix",suffix);
		insertParam.addAttr("applyId", applyId);
		insertParam.addAttr("videoName", realName);
		byte[] data = Base64.getDecoder().decode(context);
		insertParam.addAttr("applyId", applyId);
		result=RemoteInvoke.getInstance().call(insertParam);
		FTPDealUtil.fileUpload(data, path, realName);
		result.putAttr("videoId",result.getAttr("videoId"));
		result.putAttr("resourcesUrl", AppProperties.getProperties("resourcesUrl"));
		result.putAttr("path", path) ;
		result.putAttr("videoName", realName) ;
		return result;
	}

	*//**
	 * 删除资料
	 * @param path
	 * @return
	 * @throws Exception 
	 *//*
	public static boolean delFile(String path,String fileName) throws Exception {
		boolean deleteFile = FTPDealUtil.deleteFile(path, fileName);
		return deleteFile;
	}

	*//**
	 * 生成一个随机文件名
	 * @param applyId
	 * @param materialType
	 * @return
	 *//*
	public static  String getRandomName(String applyId,String materialType){
		String code=null;
		String uuid = StringUtil.getUUID();
		code=applyId+"_"+materialType+"_"+uuid;
		return code;
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println(StringUtil.getUUID());
		}
	}
	*/
}
