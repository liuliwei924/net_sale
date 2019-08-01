package org.llw.common.web.util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.llw.com.exception.SysException;
import org.llw.com.util.StringUtil;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import lombok.extern.slf4j.Slf4j;
 
@Slf4j
public class QRCodeUtils {
	/**logo 图片是占总图片的几分之一**/
	private static final int LOGO_RATIO = 5;
	private QRCodeUtils() {
		
	}
	/***
	 * 生成二维码
	 * @param ot 输出流
	 * @param url 图片的URL地址
	 * @param size 图片的大小
	 */
	public static void generateQRCode(OutputStream ot, String url,int size) {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType,String> hints = new HashMap<EncodeHintType,String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix bitMatrix;
		try {
			bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE,
					size, size, hints);
			MatrixToImageWriter.writeToStream(bitMatrix, "jpg", ot);
		} catch (Exception e) {
			log.error("generateQRCode error!",e);
			throw new SysException(e.getMessage());
		}
	}
	
	/***
	 * 生成二维码
	 * @param ot 输出流
	 * @param url 二维码的地址
	 * @param logoPath logo图片的地址
	 * @param size
	 */
	public static void generateQRCode(OutputStream ot, String url,String logoPath,int size) {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType,Object> hints = new HashMap<EncodeHintType,Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix;
        ByteArrayOutputStream out = null;
		ByteArrayInputStream imageIn = null;
		try {
			bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE,
					size, size, hints);
			out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(bitMatrix, "jpg", out);
			imageIn = new ByteArrayInputStream(out.toByteArray());
			setLogoImage(imageIn,logoPath,ot);
		} catch (Exception e) {
			log.error("generateQRCode error!",e);
			throw new SysException(e.getMessage());
		}
	}
	
	/***
	 * 生成二维码,不生成临时文件
	 * @param ot 输出流
	 * @param url 二维码的地址
	 * @param logoPath logo图片的地址
	 * @param size
	 */
	public static void generateQRCodeByStream(OutputStream ot, String url,String logoPath,int size) {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType,Object> hints = new HashMap<EncodeHintType,Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
		BitMatrix bitMatrix;
		ByteArrayOutputStream out = null;
		ByteArrayInputStream imageIn = null;
		try {
			bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE,
					size, size, hints);
			out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(bitMatrix, "jpg", out);
			imageIn = new ByteArrayInputStream(out.toByteArray());
			setLogoImage(imageIn,logoPath,ot);
		} catch (Exception e) {
			log.error("generateQRCode error!",e);
			throw new SysException(e.getMessage());
		}
		finally {
			FileUtil.closeQuietly(imageIn);
			FileUtil.closeQuietly(out);
		}
	}
	
	/** 生成二维码
	 * @param ot 输出流
	 * @param url 二维码的地址
	 * @param logoPath logo图片的地址
	 * @param size
	 */
	public static File getQRCode(String url,String logoPath,int size) {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType,Object> hints = new HashMap<EncodeHintType,Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
		BitMatrix bitMatrix;
		try {
			bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE,
					size, size, hints);
			String imagePath =  "/mnt/web/"+ UUID.randomUUID()+ ".jpg";
			File file = new File(imagePath);
			OutputStream ot = new FileOutputStream(imagePath);
			MatrixToImageWriter.writeToFile(bitMatrix, "jpg", file);
			setLogoImage(imagePath,logoPath,ot);
			ot.flush();
			ot.close();
			return file;
		} catch (Exception e) {
			log.error("getQRCode error!",e);
			throw new SysException(e.getMessage());
		}
	}
	
	/** 生成二维码图片(兼容测试环境不产生图片二维码)
	 * @param url 二维码的地址
	 * @param logoPath logo图片的地址
	 * @param size
	 */
	public static File getQRCodeNew(String url,String logoPath,int size) {
		try {
			String imagePath = "/mnt/web/"+ UUID.randomUUID()+ ".jpg";
			File file = new File(imagePath);
			OutputStream ot = new FileOutputStream(imagePath);
			generateQRCode(ot, url, logoPath, size);
			ot.flush();
			ot.close();
			return file;
		} catch (Exception e) {
			log.error("generateQRCode error!",e);
			throw new SysException(e.getMessage());
		}
	}
	
	/***
	 * 生成通讯录头像二维码
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static File geneCardQRCode(Map<String,Object> custInfo){
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map hints = new HashMap();
	    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
	    StringBuffer strBuff = new StringBuffer();
	    strBuff.append("BEGIN:VCARD\nVERSION:3.0\n");
	    if(!StringUtils.isEmpty(custInfo.get("nickName"))){
	    	strBuff.append("N:" + custInfo.get("nickName") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("email"))){
	    	strBuff.append("EMAIL:" + custInfo.get("email") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("telephone"))){
	    	strBuff.append("TEL:" + custInfo.get("telephone") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("address"))){
	    	strBuff.append("ADR;WORK:" + custInfo.get("address") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("cmpName"))){
	    	strBuff.append("ORG:" + custInfo.get("cmpName") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("cmpJob"))){
	    	strBuff.append("TITLE:" + custInfo.get("cmpJob") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("custDesc"))){
	    	strBuff.append("NOTE:" + custInfo.get("custDesc") +"\n");
	    }
	    strBuff.append("END:VCARD");
	    try {    	
	    	String logoPath = StringUtil.getString(custInfo.get("logoPath"));
	    	BitMatrix bitMatrix = multiFormatWriter.encode(strBuff.toString(), BarcodeFormat.QR_CODE, 800, 800, hints);
			String sysPath = "/mnt/web";
			String otImagePath = sysPath + "/"+ UUID.randomUUID()+ ".jpg";
			File file = new File(otImagePath);
			OutputStream ot = new FileOutputStream(otImagePath);
			
			String imagePath = sysPath + "/"+ UUID.randomUUID()+ ".jpg";
			MatrixToImageWriter.writeToFile(bitMatrix, "jpg", new File(imagePath));
			
			// 设置头像 设置logo
			BufferedImage image = ImageIO.read(new File(imagePath));
			BufferedImage logo = ImageIO.read(new File(logoPath));
			setLogoImage(image,logo,ot);
			FileUtil.deleteQuietly(new File(imagePath));
			ot.flush();
			ot.close();
			return file;
		} catch (Exception e) {
			log.error("geneCardQRCode error!",e);
			throw new SysException(e.getMessage());
		}
	}
	
	/**
	 * 生成二维码名片
	 * @param custInfo
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String generateQRCode(Map<String,Object> custInfo) {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map hints = new HashMap();
	    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
	    String content = "BEGIN:VCARD\n" +
	    	    "VERSION:3.0\n" +
	    	    "N:" + custInfo.get("nickName") +"\n" +
	    	    "EMAIL:" + custInfo.get("email") + "\n" +
	    	    "TEL:" + custInfo.get("telephone") + "\n" +
	    	    "ADR:" + custInfo.get("address") + "\n" +
	    	    "ORG:" + custInfo.get("cmpName") + "\n" +
	    	    "TITLE:" + custInfo.get("cmpJob") + "\n" +
	    	    "NOTE:"+ custInfo.get("custDesc") + "\n" +
	    	    "END:VCARD";
		try {
			BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 800, 800, hints);
			String imagePath = "/mnt/web/"+ UUID.randomUUID()+ ".jpg";
			File file1 = new File(imagePath); 
			MatrixToImageWriter.writeToFile(bitMatrix, "jpg", file1);
			
			try {
				file1.delete();
			} catch (Exception e) {
				log.error("delete file：" +imagePath,e);
			}
			
			return imagePath;
		} catch (Exception e) {
			log.error("generateQRCode error!",e);
			throw new SysException(e.getMessage());
		}
	}
	
	/**
	 * 生成透明背景二维码
	 * @param custInfo
	 * @param rgb
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String generateQRCodeAlpha(Map<String,Object> custInfo, String rgb) {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map hints = new HashMap();
	    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
	    StringBuffer sb = new StringBuffer();
	    sb.append("BEGIN:VCARD\nVERSION:3.0\n");
	    if(!StringUtils.isEmpty(custInfo.get("nickName"))){
	    	sb.append("N:" + custInfo.get("nickName") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("email"))){
	    	sb.append("EMAIL:" + custInfo.get("email") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("telephone"))){
	    	sb.append("TEL:" + custInfo.get("telephone") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("address"))){
	    	sb.append("ADR:" + custInfo.get("address") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("cmpName"))){
	    	sb.append("ORG:" + custInfo.get("cmpName") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("cmpJob"))){
	    	sb.append("TITLE:" + custInfo.get("cmpJob") +"\n");
	    }
	    if(!StringUtils.isEmpty(custInfo.get("custDesc"))){
	    	sb.append("NOTE:" + custInfo.get("custDesc") +"\n");
	    }
	    sb.append("END:VCARD");
		try {
			String[] colors = StringUtils.trimArrayElements(rgb.split(","));
			int r = Integer.valueOf(colors[0]);
			int g = Integer.valueOf(colors[1]);
			int b = Integer.valueOf(colors[2]);
			int foreColor = ((0xFF << 24) | (r << 16) | (g << 8) | b);
			BitMatrix bitMatrix = multiFormatWriter.encode(sb.toString(), BarcodeFormat.QR_CODE, 800, 800, hints);
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = image.createGraphics();
			image = g2d.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
			g2d.dispose();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if(bitMatrix.get(x, y)){
						image.setRGB(x, y, foreColor);
					}else{
						image.setRGB(x, y, 0x00ffffff);
					}
				}
			}
			String imagePath = "/mnt/web/" + UUID.randomUUID().toString() + ".png";
			
			File file1 = new File(imagePath); 
			
			ImageIO.write(image, "png", new File(imagePath));
			
			try {
				file1.delete();
			} catch (Exception e) {
				log.error("delete file：" +imagePath,e);
			}
			return imagePath;
		} catch (Exception e) {
			log.error("generateQRCodeAlpha error!",e);
			throw new SysException(e.getMessage());
		}
	}
	
	/***
	 * 
	 * @param image
	 * @param logo
	 * @param ot
	 * @throws IOException
	 */
	public static void setLogoImage(BufferedImage image,BufferedImage logo,OutputStream ot) throws IOException{
		Graphics2D g = image.createGraphics();
		// 考虑到logo照片贴到二维码中，建议大小不要超过二维码的1/5;
		int oldWidth = logo.getWidth();
		int oldHeight = logo.getHeight();
		int width = image.getWidth() / LOGO_RATIO;
		int height = image.getHeight() / LOGO_RATIO;
		width = width > oldWidth ? oldWidth : width;
		height = height > oldHeight ? oldHeight : height;
		// logo起始位置，应该为  总大小-logo图片大小 除以2
		int x = (image.getWidth() - width) / 2;
		int y = (image.getHeight() - height) / 2;
		// 绘制图
		g.drawImage(logo, x, y, width, height, null);
		// 给logo画边框
		g.setStroke(new BasicStroke(0));//边框大小
		g.setColor(Color.WHITE);//边框颜色
		g.drawRect(x, y, width, height);
		g.dispose();
		// 写入logo照片到二维码
		ImageIO.write(image, "jpg", ot);
	}
	
	
	private static void setLogoImage(String imagePath,String logoPath,OutputStream ot) throws IOException{
		;
		BufferedImage logo = ImageIO.read(new DefaultResourceLoader().getResource(logoPath).getInputStream());
		BufferedImage image = ImageIO.read(new File(imagePath));
		Graphics2D g = image.createGraphics();
		// 考虑到logo照片贴到二维码中，建议大小不要超过二维码的1/5;
		int oldWidth = logo.getWidth();
		int oldHeight = logo.getHeight();
		int width = image.getWidth() / LOGO_RATIO;
		int height = image.getHeight() / LOGO_RATIO;
		width = width > oldWidth ? oldWidth : width;
		height = height > oldHeight ? oldHeight : height;
		// logo起始位置，应该为  总大小-logo图片大小 除以2
		int x = (image.getWidth() - width) / 2;
		int y = (image.getHeight() - height) / 2;
		// 绘制图
		g.drawImage(logo, x, y, width, height, null);
		// 给logo画边框
		g.setStroke(new BasicStroke(0));//边框大小
		g.setColor(Color.WHITE);//边框颜色
		g.drawRect(x, y, width, height);
		g.dispose();
		// 写入logo照片到二维码
		ImageIO.write(image, "jpg", ot);
	}
	
	private static void setLogoImage(InputStream imageIn,String logoPath,OutputStream ot) throws IOException{
		BufferedImage logo = null;
		if(!StringUtils.isEmpty(logoPath) && logoPath.contains("classpath:")){
			logo = ImageIO.read(new DefaultResourceLoader().getResource(logoPath).getInputStream());
		}else{
			logo = ImageIO.read(new File(logoPath));
		}
		BufferedImage image = ImageIO.read(imageIn);
		Graphics2D g = image.createGraphics();
		// 考虑到logo照片贴到二维码中，建议大小不要超过二维码的1/5;
		int oldWidth = logo.getWidth();
		int oldHeight = logo.getHeight();
		int width = image.getWidth() / LOGO_RATIO;
		int height = image.getHeight() / LOGO_RATIO;
		width = width > oldWidth ? oldWidth : width;
		height = height > oldHeight ? oldHeight : height;
		// logo起始位置，应该为  总大小-logo图片大小 除以2
		int x = (image.getWidth() - width) / 2;
		int y = (image.getHeight() - height) / 2;
		// 绘制图
		g.drawImage(logo, x, y, width, height, null);
		// 给logo画边框
		g.setStroke(new BasicStroke(0));//边框大小
		g.setColor(Color.WHITE);//边框颜色
		g.drawRect(x, y, width, height);
		g.dispose();
		// 写入logo照片到二维码
		ImageIO.write(image, "jpg", ot);
	}
	
	
	public static void main(String[] args){
//		File file = new File("c://info.jpg");
//		OutputStream ot;
		try {
//			ot = new FileOutputStream(file);
//			QRCodeUtils.generateQRCode(ot,
//					"http://192.168.2.52/phoneUserHome.do",
//					"D:/work/duoduo-licai/duoduo-licai-show/src/main/webapp/WEB-INF/config/ddzf.jpg",
//					300);
//			ot.flush();
//			ot.close();
			Map<String,Object> custInfo = new HashMap<String, Object>();
			custInfo.put("nickName", "雷小峰");
			custInfo.put("email", "12345678@qq.com");
			custInfo.put("telephone", "13512345678");
			custInfo.put("address", "福田区彩田路新浩壹都2404");
			custInfo.put("cmpName", "多多智富");
			custInfo.put("cmpJob", "信贷经理");
			custInfo.put("custDesc", "信贷高级助手");
			generateQRCodeAlpha(custInfo, "227,38,38");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
