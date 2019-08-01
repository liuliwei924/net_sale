package org.llw.common.web.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.llw.com.exception.AppException;
import org.llw.com.exception.SysException;
import org.llw.com.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {
	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	/***
	 * 将fileName的文件内容变为list String
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException 
	 */
	public static List<String> readFileToLines(String fileName) throws IOException {
		List<String> lines = new LinkedList<String>();
		String line = "";
	
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		while ((line = in.readLine()) != null) {
			lines.add(line);
		}
		closeQuietly(in);
		
		return lines;
	}

	/***
	 * 通过InputStream 获取数据
	 * @param InputStream
	 * @return
	 */
	public static String getStrByInputStream(InputStream input) {
		BufferedReader reader =null;
		try {
			reader = new BufferedReader(new InputStreamReader(input, "UTF-8")); // 实例化输入流，并获取网页代码
			String s; // 依次循环，至到读的值为空
			StringBuilder sb = new StringBuilder();
			while ((s = reader.readLine()) != null) {
				sb.append(s);
			}
			return sb.toString();
		} catch (Exception e) {
			log.error("get input stream error:",e);
			throw new SysException("获取InputStream 出错：" + e.getMessage());
		}finally {
			FileUtil.closeQuietly(reader);
		}
	}
	/**
	 * write data 
	 * @param data
	 * @param output
	 * @throws IOException
	 */
    public static void write(byte[] data, Writer output) throws IOException {
        if (data != null) {
        	output.write(new String(data, "UTF-8"));
        }
    }
    
    /***
	 * delete
	 * 
	 * @param file
	 */
	public static void deleteQuietly(File file) {
		if(file.exists()){
			file.delete();
		}
		
	}
	
    /***
     * copy
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

	/***
	 * close
	 * 
	 * @param closeable
	 */
	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}
	/**
	 * make file Director
	 * 
	 * @param path
	 */
	public static void fileDirMake(String path) {
		//
		File fileDir = new File(path);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
	}
	
	/***
	 * get MultipartFile
	 * 
	 * @param request
	 * @return
	 * @throws WiFiException
	 */
	public static MultipartFile getUploadFiles(MultipartHttpServletRequest request,String fileTypes){
		Map<String, MultipartFile> fileMaps = request.getFileMap();
		for (String fileName:fileMaps.keySet()) {
			MultipartFile file = fileMaps.get(fileName);
			if (file.getSize() <= 0) {
				continue;
			}
			String fileType = "";
			String originaFileName = file.getOriginalFilename();
			if(originaFileName.lastIndexOf(".")>0){
				fileType = originaFileName.substring(originaFileName
						.lastIndexOf("."));
			}
			if (!fileTypes.toLowerCase().contains(fileType.substring(1).toLowerCase())) {
				throw new AppException("File Type not Suport,上传文件类型不支持:" + fileType + "!");
			}
			return file;
		}
		return null;
	}
	
	
	/***
	 * file upload to local
	 * @param request
	 * @return
	 * @throws WiFiException
	 */
	public static String updateFileToLocal(MultipartFile file,String path) throws Exception {
		String originaFileName = file.getOriginalFilename();
		String fileType = "";
		if(originaFileName.lastIndexOf(".")>0){
			fileType = originaFileName.substring(originaFileName.lastIndexOf("."));
		}
		String realName = StringUtil.getUUID();
		String saveName = realName + fileType;
		log.info("file upload to path=" + path + saveName);
		file.transferTo(new File(path + saveName));
		return saveName;
	}
	
}
