package org.xxjr.store.web.action.common;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ddq.common.context.AppProperties;
import org.ddq.common.context.AppResult;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.llw.common.web.util.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xxjr.sys.util.FileGroupUtil;
import org.xxjr.tools.util.QcloudUploader;

/**
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/uploadAction/")
public class UpfileAction {

	/***
	 * 文件上传
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("uploadFile")
	@ResponseBody
	public Map<String, Object> uploadFile(MultipartHttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		String fileType = request.getParameter("fileType");
		// 日志信息
		AppResult context = new AppResult();
		try {
			if("fxPoster".equals(fileType)){
				uploadLocalSave(request, map, fileType);
			}else{
				uploadSave(request, map, fileType);
			}
		} catch (Throwable e) {
			LogerUtil.error(UpfileAction.class, e,
					"uploadAction uplaodimage error");
			ExceptionUtil.setExceptionMessage(e, context,
					DuoduoSession.getShowLog());
			map.put("state", "invalid");
			map.put("statusText", context.getMessage());
		}
		return map;
	}

	/***
	 * 执行文件 上传处理
	 * 
	 * @param request
	 * @param map
	 * @param fileType
	 * @throws Throwable
	 */
	public static void uploadSave(MultipartHttpServletRequest request,
			Map<String, Object> map, String fileType) throws Throwable {
		String fileTypes = FileGroupUtil.getFileTypes(fileType);
    	MultipartFile file = FileUtil.getUploadFiles(request, fileTypes);
		InputStream inputStream = file.getInputStream();
    	String contentType = file.getContentType();
    	long fileSize = file.getSize();
    	String desCosPath = "/upfile/" + fileType + "/" 
    			+ DateUtil.toStringByParttern(new Date(), DateUtil.DATE_PATTERN_YYYY_MM_DD) + "/";
    	String originaFileName = file.getOriginalFilename();
		String uploadFileType = ".png";
		if(originaFileName.lastIndexOf(".") > 0){
			uploadFileType = originaFileName.substring(originaFileName.lastIndexOf("."));
		}
    	String saveName = StringUtil.getUUID() + uploadFileType;
    	QcloudUploader.createDirOnNotExists(desCosPath);
		boolean isSuccess = QcloudUploader.uploadFile(desCosPath + saveName, inputStream, fileSize, contentType);
		if(isSuccess){
			map.put("fileId", "https://static.xxjr.com/" + desCosPath + saveName);
			map.put("fileType", fileType);
			map.put("state", "SUCCESS");
		} else {
			map.put("state", "invalid");
			map.put("statusText", "文件上传不成功，请重新上传");
		}
		if(inputStream != null){
			inputStream.close();
		}
	}
	
	/**
	 * 上传到本地服务器
	 * @param request
	 * @param map
	 * @param fileType
	 * @throws Throwable
	 */
	public static void uploadLocalSave(MultipartHttpServletRequest request,Map<String, Object> map, String fileType) throws Throwable {
		String fileTypes = FileGroupUtil.getFileTypes(fileType);
    	MultipartFile file = FileUtil.getUploadFiles(request, fileTypes);
    	String uploadPath = AppProperties.getProperties("upload.path.image");
    	String relativePath = "/images/" + fileType + "/";
    	FileUtil.fileDirMake(uploadPath + relativePath);
		String saveName = FileUtil.updateFileToLocal(file, uploadPath + relativePath);
		if(!StringUtils.isEmpty(saveName)){
			map.put("fileId", relativePath + saveName);
			map.put("state", "SUCCESS");
		} else {
			map.put("state", "invalid");
			map.put("statusText", "文件上传不成功，请重新上传");
		}
	}
	
}
