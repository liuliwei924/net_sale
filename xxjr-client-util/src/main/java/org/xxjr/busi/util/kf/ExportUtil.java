package org.xxjr.busi.util.kf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.ddq.common.context.AppParam;
import org.ddq.common.context.AppResult;
import org.ddq.common.core.service.SoaManager;
import org.ddq.common.exception.AppException;
import org.ddq.common.exception.ErrorCode;
import org.ddq.common.exception.ExceptionUtil;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.DateUtil;
import org.ddq.common.util.JsonUtil;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.NumberUtil;
import org.ddq.common.util.StringUtil;
import org.ddq.common.web.session.DuoduoSession;
import org.ddq.common.web.session.RequestUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xxjr.sys.util.ValidUtils;

import com.alibaba.fastjson.JSONArray;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExportUtil {
	/***
	 * 
	 * 导出excel
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void  exportExcel(HttpServletRequest request,HttpServletResponse response){
		AppResult result = new AppResult();
		String service=(String)request.getAttribute("service");
		String method = (String)request.getAttribute("method");
		try{
			AppParam params = new AppParam();
			RequestUtil.setAttr(params, request);
			if(StringUtils.isEmpty(params.getAttr("exportTitles"))){
				PrintWriter out = response.getWriter();
				out.print("not found params:exportTitles");
				out.flush();
				return;
			}
			String titleNames=params.removeAttr("exportTitles").toString();
			LinkedHashMap<String, String> exchangeTitle = new LinkedHashMap<String, String>();
			JSONArray titleJson = JSONArray.parseArray(titleNames);
			for (int i = 0; i < titleJson.size(); i++) {
				Map<String,String> titleMap=(Map<String,String>)titleJson.get(i);
				if(titleMap.containsKey("title")&&titleMap.containsKey("name")){
					exchangeTitle.put(titleMap.get("title"),titleMap.get("name"));
				}
			}
			if(!StringUtils.isEmpty(params.getAttr("exportParams"))){
				String exportParams= params.removeAttr("exportParams").toString();
				Map<String,Object> mapParams = JsonUtil.getInstance().json2Object(exportParams, Map.class);
				params.addAttrs(mapParams);
			}
			params.setEveryPage(5000);
			params.setService(service);
			params.setMethod(method);
			result = SoaManager.getInstance().callNoTx(params);
			if(!StringUtils.isEmpty(params.getAttr("data_dictionary"))){
				String dictionary=params.removeAttr("data_dictionary").toString();
				Map<String,Map<String,String>> map=JsonUtil.getInstance().json2Object(dictionary, Map.class);
				List<Map<String,Object>> list=result.getRows();
				for (int i = 0; i < list.size(); i++) {
					Map<String,Object> obj=list.get(i);
					 for (String key: map.keySet()) {
							if(obj.containsKey(key)){
								String value= map.get(key).get(obj.get(key).toString());
								if(value.indexOf("f_")==0){
									String format[] =value.split("_");
									value=obj.get(format[1]).toString()+format[2];
								}
								obj.put(key, value);
								list.set(i, obj);
							}
					}
				}
				result.setRows(list);
			}
			
			String fileName = DateUtil.toStringByParttern(new Date(),
        			DateUtil.DATE_PATTERNYYYYMMDDHHMMSSSSS) + ".xls";
			response.reset();// 清空输出流
			response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			// 设定输出文件头
			response.setContentType("application/msexcel");// 定义输出类型
			writeExcel(response.getOutputStream(), exchangeTitle, result.getRows());
		}catch (Exception e) {
			
			LogerUtil.error(ExportUtil.class, e,"CommExportAction exportExcel error");
			ExceptionUtil.setExceptionMessage(e, result, DuoduoSession.getShowLog());
			PrintWriter out;
			try {
				out = response.getWriter();
				out.print("error exportExcel:" + e.getMessage());
				out.flush();
			} catch (IOException e1) {
			}
			
		}
	}
	
	/**
	 * 生成excel
	 * @param response
	 * @param titleMap
	 * @param rows
	 * @throws Exception
	 */
	public static void writeExcel(OutputStream os,
			LinkedHashMap<String, String> titleMap,
			List<Map<String, Object>> rows) throws Exception {
		WritableWorkbook workbook = getWorkbook(os, titleMap, rows);
		workbook.write();
		workbook.close();
	}
	/**
	 * 生成excel
	 * @param response
	 * @param titleMap
	 * @param rows
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void writeManySheetExcel(OutputStream os,
			LinkedHashMap<String, String> titleMap,
			Map<String, String> sheetTitle, Map<String, Object> map)
			throws Exception {
		WritableWorkbook workbook = Workbook.createWorkbook(os);
		
		for (int i = 0; i < map.size(); i++) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("data"+i);
			workbook = createManySheet(workbook, titleMap, list,
					sheetTitle.get("title_" + i), i);
		}
		workbook.write();
		workbook.close();
	}
	/**
	 * 获取workBook 可以做特殊定制
	 * @param response
	 * @param titleMap
	 * @param rows
	 * @throws Exception
	 */
	public static WritableWorkbook getWorkbook(OutputStream os,
			LinkedHashMap<String, String> titleMap,
			List<Map<String, Object>> rows) throws Exception {
		WritableWorkbook workbook = Workbook.createWorkbook(os);
		WritableSheet sheet = workbook.createSheet("Sheet1", 0);

		// 设置纵横打印（默认为纵打）、打印纸
		jxl.SheetSettings sheetset = sheet.getSettings();
		sheetset.setProtected(false);

		// 单元格字体
		WritableFont NormalFont = new WritableFont(WritableFont.ARIAL, 10);
		WritableFont BoldFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD);

		// 用于标题居中
		WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);
		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
		wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
		wcf_center.setWrap(false); // 文字是否换行

		// 用于正文居左
		WritableCellFormat wcf_left = new WritableCellFormat(NormalFont);
		wcf_left.setBorder(Border.NONE, BorderLineStyle.THIN); // 线条
		wcf_left.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_left.setAlignment(Alignment.LEFT); // 文字水平对齐
		wcf_left.setWrap(false); // 文字是否换行

		// 以下是EXCEL开头大标题，暂时省略
		// sheet.mergeCells(0, 0, colWidth, 0);
		// sheet.addCell(new Label(0, 0, "XX报表", wcf_center));
		// 第一行列标题
		List<String> titles = new ArrayList<String>(titleMap.keySet());
		List<String> titleVal = new ArrayList<String>(titleMap.values());
		for (int i = 0; i < titles.size(); i++) {
			sheet.addCell(new Label(i, 0, titles.get(i), wcf_center));
		}
		// EXCEL正文数据
		int i = 1;
		String titleKey = null;
		for (Map<String, Object> row : rows) {
			for (int j = 0; j < titles.size(); j++) {
				titleKey = titleVal.get(j);
				sheet.addCell(new Label(j, i, StringUtil.objectToStr(row
						.get(titleKey)), wcf_left));
			}
			i++;
		}
		
		return workbook;
	}
	/**
	 * 追加数据Excle
	 * @param file
	 * @param titleMap
	 * @param rows
	 * @throws Exception
	 */
	public static void writeExcel(File file,LinkedHashMap<String, String> titleMap,
			List<Map<String, Object>> rows) throws Exception {
		Workbook book = Workbook.getWorkbook(file);
		Sheet sheet = book.getSheet(0);
		// 获取行
		int length = sheet.getRows();
		System.out.println(length);
		WritableWorkbook wbook = Workbook.createWorkbook(file, book); // 根据book创建一个操作对象
		WritableSheet sh = wbook.getSheet(0);// 得到一个工作对象
		
		// 单元格字体
		WritableFont NormalFont = new WritableFont(WritableFont.ARIAL, 10);
		WritableFont BoldFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD);
		// 用于标题居中
		WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);
		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
		wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
		wcf_center.setWrap(false); // 文字是否换行

		// 用于正文居左
		WritableCellFormat wcf_left = new WritableCellFormat(NormalFont);
		wcf_left.setBorder(Border.NONE, BorderLineStyle.THIN); // 线条
		wcf_left.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_left.setAlignment(Alignment.LEFT); // 文字水平对齐
		wcf_left.setWrap(false); // 文字是否换行
		
		
		List<String> titleVal = new ArrayList<String>(titleMap.values());
		List<String> titles = new ArrayList<String>(titleMap.keySet());
		if (length == 0) {
			for (int i = 0; i < titles.size(); i++) {
				sh.addCell(new Label(i, 0, titles.get(i), wcf_center));
			}
			length++;
		}
		// 从最后一行开始加 EXCEL正文数据
		int i = length;
		String titleKey = null;
		for (Map<String, Object> row : rows) {
			for (int j = 0; j < titles.size(); j++) {
				titleKey = titleVal.get(j);
				sh.addCell(new Label(j, i, StringUtil.objectToStr(row
						.get(titleKey)),wcf_left));
			}
			i++;
		}
		wbook.write();
		wbook.close();
	}
	
	public static List<Map<String,Object>> readExcel(MultipartHttpServletRequest multipartRequest) throws Exception {
		String keys[] =multipartRequest.getParameter("keys").split(",");
		return readExcel(keys, multipartRequest);
	}
	
	public static List<Map<String,Object>> readExcel(String[] keys,MultipartHttpServletRequest multipartRequest) throws Exception {
		Map<String, MultipartFile> fileMaps = multipartRequest.getFileMap();
		InputStream inputS = getInputStream(fileMaps);
		String fileType = getFileType(fileMaps);
		if(inputS==null || fileType == null){
			throw new SysException("导入文件失败");
		}
		List<Map<String,Object>> list = null;
		if (".XLS".equals(fileType.toUpperCase())) {
			list = ImportExcleUtil.xlsParse(keys,inputS);
		} else if(".XLSX".equals(fileType.toUpperCase())){
			list = ImportExcleUtil.xlsxParse(keys,inputS);
		} else {
			throw new SysException("不支持的文件类型");
		}
		if(list.size() == 0){
			throw new AppException(ErrorCode.FILE_UPLOAD_FAILD_FILE_EMPTY);
		}
		return list;
	}
	
	private static InputStream  getInputStream(Map<String, MultipartFile> fileMaps) 
			throws IOException{
		for (String fileName:fileMaps.keySet()) {
			MultipartFile file = fileMaps.get(fileName);
			if (file.getSize() != 0) {
				return file.getInputStream();
			}
		}
		return null;
	}
	
	private static String  getFileType(Map<String, MultipartFile> fileMaps) 
			throws IOException{
		for (String fileName:fileMaps.keySet()) {
			MultipartFile file = fileMaps.get(fileName);
			if (file.getSize() != 0) {
				String originaFileName = file.getOriginalFilename();
				String fileType="";
				if(originaFileName.lastIndexOf(".")>0){
					fileType = originaFileName.substring(originaFileName
							.lastIndexOf("."));
				}
				return fileType;
			}
		}
		return null;
	}
	
	/**
	 * 当大批量数据导出时，创建多个sheet进行导出
	 * @param response
	 * @param titleMap
	 * @param rows
	 * @throws Exception
	 */
	public static WritableWorkbook createManySheet(WritableWorkbook workbook,
			LinkedHashMap<String, String> titleMap,List<Map<String, Object>> rows,int index) throws Exception {
		WritableSheet sheet = workbook.createSheet("Sheet" + index, index);
		// 设置纵横打印（默认为纵打）、打印纸
		jxl.SheetSettings sheetset = sheet.getSettings();
		sheetset.setProtected(false);

		// 单元格字体
		WritableFont NormalFont = new WritableFont(WritableFont.ARIAL, 10);
		WritableFont BoldFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD);

		// 用于标题居中
		WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);
		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
		wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
		wcf_center.setWrap(false); // 文字是否换行

		// 用于正文居左
		WritableCellFormat wcf_left = new WritableCellFormat(NormalFont);
		wcf_left.setBorder(Border.NONE, BorderLineStyle.THIN); // 线条d
		wcf_left.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_left.setAlignment(Alignment.LEFT); // 文字水平对齐
		wcf_left.setWrap(false); // 文字是否换行

		// 第一行列标题
		List<String> titles = new ArrayList<String>(titleMap.keySet());
		List<String> titleVal = new ArrayList<String>(titleMap.values());
		// EXCEL正文数据
		int i = 0;//导出数据从第一行开始,若从第二行开始i改为1即可
		String titleKey = null;
		for (Map<String, Object> row : rows) {
			for (int j = 0; j < titles.size(); j++) {
				titleKey = titleVal.get(j);
				sheet.addCell(new Label(j, i, StringUtil.objectToStr(row.get(titleKey)), wcf_left));
			}
			i++;
		}
		return workbook;
	}
	
	/**
	 * 当大批量数据导出时，创建多个sheet进行导出
	 * @param response
	 * @param titleMap
	 * @param rows
	 * @throws Exception
	 */
	public static WritableWorkbook createManySheet(WritableWorkbook workbook,
			LinkedHashMap<String, String> titleMap,List<Map<String, Object>> rows,String title,int index) throws Exception {
		WritableSheet sheet = workbook.createSheet(title, index);
		// 设置纵横打印（默认为纵打）、打印纸
		jxl.SheetSettings sheetset = sheet.getSettings();
		sheetset.setProtected(false);

		// 单元格字体
		WritableFont NormalFont = new WritableFont(WritableFont.ARIAL, 10);
		WritableFont BoldFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD);

		// 用于标题居中
		WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);
		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
		wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
		wcf_center.setWrap(false); // 文字是否换行

		// 用于正文居左
		WritableCellFormat wcf_left = new WritableCellFormat(NormalFont);
		wcf_left.setBorder(Border.NONE, BorderLineStyle.THIN); // 线条d
		wcf_left.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_left.setAlignment(Alignment.LEFT); // 文字水平对齐
		wcf_left.setWrap(false); // 文字是否换行

		// 第一行列标题
		List<String> titles = new ArrayList<String>(titleMap.keySet());
		List<String> titleVal = new ArrayList<String>(titleMap.values());
		for (int i = 0; i < titles.size(); i++) {
			sheet.addCell(new Label(i, 0, titles.get(i), wcf_center));
		}
		// EXCEL正文数据
		int i = 1;//导出数据从第一行开始,若从第二行开始i改为1即可
		String titleKey = null;
		for (Map<String, Object> row : rows) {
			for (int j = 0; j < titles.size(); j++) {
				titleKey = titleVal.get(j);
				sheet.addCell(new Label(j, i, StringUtil.objectToStr(row.get(titleKey)), wcf_left));
			}
			i++;
		}
		return workbook;
	}
	
	
	 /**
     * 读取借贷人信息
     * @param request
     * @return
     * @throws Exception
     */
	public static List<Map<String,Object>> readDebtorDtlExcel(MultipartHttpServletRequest multipartRequest) throws Exception {
		Map<String, MultipartFile> fileMaps = multipartRequest.getFileMap();
		InputStream inputS = getInputStream(fileMaps);
		String fileType = getFileType(fileMaps);
		if(inputS==null || fileType == null){
			throw new SysException("导入文件失败");
		}
		Map<String, String> map= new HashMap<String,String>(); 
		map.put("合同编号", "compactNo");
		map.put("客户", "userName");
		map.put("身份证号码", "cardNo");
		map.put("联系电话","telephone");
		map.put("联系电话2","telephone2");
		map.put("身份证地址","cardAddress");
		map.put("物业地址","estateAddress");
		map.put("银行卡信息", "bankNo");
		map.put("负责门店", "orgName");
		map.put("客户经理", "orgManager");
		map.put("贷款金额", "totalAmt");
		map.put("计划还款期数（月）", "totalRepNum");
		map.put("还款方式", "repayName");//repayType
		map.put("利息（分/月）", "fee");
		map.put("每月应还本金", "principalAmt");
		map.put("每月应还利息", "interestAmt");
		map.put("每月还款日", "repayDay");
		map.put("第一次还款日", "repayDate");
		map.put("最近一次还款日期", "lateDate");
		map.put("下期应还款日", "nextRepayDate");
		map.put("放款日期1", "lendingDate");
		map.put("放款日期2", "lendingDate2");
		map.put("城市名称", "cityName");
		map.put("备注", "remarks");
		map.put("beginRowIndex", "3");
		List<Map<String,Object>> list = null;
		if (".XLS".equals(fileType.toUpperCase())) {
			list = ImportExcleUtil.xlsParse(inputS, map);
		} else if(".XLSX".equals(fileType.toUpperCase())){
			list = ImportExcleUtil.xlsxParse(inputS, map);
		} else {
			throw new SysException("不支持的文件类型");
		}
		
		if(list.size() == 0){
			throw new AppException(ErrorCode.FILE_UPLOAD_FAILD_FILE_EMPTY);
		}
		List<Map<String,Object>> newList = new ArrayList<Map<String,Object>>();
		Calendar calendar = new GregorianCalendar(1900,0,-1);  
		Date d = calendar.getTime();
		int size = 6;
		for(Map<String,Object> row: list){
			if(StringUtils.isEmpty(row.get("userName"))||
					StringUtils.isEmpty(row.get("telephone"))||
					StringUtils.isEmpty(row.get("totalAmt"))||
					StringUtils.isEmpty(row.get("totalRepNum"))||
					StringUtils.isEmpty(row.get("repayDate")) ||
					StringUtils.isEmpty(row.get("repayName"))){
					continue;
			}
			String telephone = StringUtil.getString(row.get("telephone"));
			if(!ValidUtils.validateTelephone(telephone)){
				throw new SysException("第:" + size +"行记录中联系电话["+ telephone +"]格式有误！");
			}
			String repayName = StringUtil.getString(row.get("repayName"));
			String fee = StringUtil.getString(row.get("fee"));
			String principalAmt = StringUtil.getString(row.get("principalAmt"));
			String interestAmt = StringUtil.getString(row.get("interestAmt"));
			String totalRepNum = StringUtil.getString(row.get("totalRepNum"));
			String repayDay = StringUtil.getString(row.get("repayDay"));
			//上传日期可能是数字需要转换格式
			String lateDate = StringUtil.getString(row.get("lateDate"));
			if (!StringUtils.isEmpty(lateDate) && NumberUtil.isDouble(lateDate)) {
				lateDate = DateUtil.getSimpleFmt(DateUtils.addDays(d, Double
						.valueOf(lateDate).intValue()));
				row.put("lateDate", lateDate);
			}
			
			String repayDate = StringUtil.getString(row.get("repayDate"));
			if (!StringUtils.isEmpty(repayDate) && NumberUtil.isDouble(repayDate)) {
				repayDate = DateUtil.getSimpleFmt(DateUtils.addDays(d, Double
						.valueOf(repayDate).intValue()));
				row.put("repayDate", repayDate);
			}
			String nextRepayDate = StringUtil.getString(row.get("nextRepayDate"));
			if (!StringUtils.isEmpty(nextRepayDate) && NumberUtil.isDouble(nextRepayDate)) {
				nextRepayDate = DateUtil.getSimpleFmt(DateUtils.addDays(d, Double
						.valueOf(nextRepayDate).intValue()));
				row.put("nextRepayDate", nextRepayDate);
			}
			String lendingDate = StringUtil.getString(row.get("lendingDate"));
			if (!StringUtils.isEmpty(lendingDate) && NumberUtil.isDouble(lendingDate)) {
				lendingDate = DateUtil.getSimpleFmt(DateUtils.addDays(d, Double
						.valueOf(lendingDate).intValue()));
				row.put("lendingDate", lendingDate);
			}
			String lendingDate2 = StringUtil.getString(row.get("lendingDate2"));
			if (!StringUtils.isEmpty(lendingDate2) && NumberUtil.isDouble(lendingDate2)) {
				lendingDate2 = DateUtil.getSimpleFmt(DateUtils.addDays(d, Double
						.valueOf(lendingDate2).intValue()));
				row.put("lendingDate2", lendingDate2);
			}
			row.put("repayType",
					repayName.equals("等额本息") ? "1" : repayName
							.equals("先息后本") ? "2" : repayName
							.equals("等本等息") ? "3" : "4");
			row.put("telephone", telephone);
			row.put("totalRepNum", NumberUtil.isDouble(totalRepNum) ? totalRepNum : 0);
			row.put("fee", NumberUtil.isDouble(fee) ? fee : 0);
			row.put("principalAmt", NumberUtil.isDouble(principalAmt) ? principalAmt : 0);
			row.put("interestAmt", NumberUtil.isDouble(interestAmt) ? interestAmt : 0);
			row.put("repayDay", repayDay.replace("号", ""));
//			String bankName = StringUtil.getString(row.get("bankName"));
//			String bankCode = getBankCode(bankName);
//			if(!StringUtils.isEmpty(bankCode)){
//				row.put("bankCode", bankCode);
//			}
			newList.add(row);
			size ++;
        }  
		return newList;
	}	

}
