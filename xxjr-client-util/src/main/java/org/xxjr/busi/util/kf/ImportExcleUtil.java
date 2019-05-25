package org.xxjr.busi.util.kf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ddq.common.exception.SysException;
import org.ddq.common.util.LogerUtil;
import org.ddq.common.util.StringUtil;
import org.springframework.util.StringUtils;

/***
 * excel 文件解析处理
 * @author Administrator
 *
 */
public class ImportExcleUtil   {
	
	
	/****
	 * 解析 xls文件 或xlsx文件
	 * @param fileName
	 * @param path
	 * @param map 表头对应的字段名
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> readExcel(String fileName, String path,Map<String,String> map)
			throws Exception {
		String filetype = fileName.substring(fileName.lastIndexOf(".") + 1);
		FileInputStream inputS =null;
		if ("XLS".equals(filetype.toUpperCase())) {
			inputS = new FileInputStream(fileName); 
			return xlsParse(inputS,map);
		} else {
			inputS = new FileInputStream(fileName); 
			return xlsxParse(inputS,map);
		}
	
	}
	/****
	 * 解析 XLS文件
	 * @param inputS 
	 * @param map 表头对就原字段名
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> xlsParse(InputStream inputS,Map<String, String>map){
		Map<Integer,String> listTitle = new HashMap<Integer,String>();
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		try {
			HSSFWorkbook workBook = null;
			try {
				workBook = new HSSFWorkbook(inputS);
			} catch (Exception ex) {
				LogerUtil.error(ImportExcleUtil.class, ex, "Parse Filee Error");
				throw new SysException("文件无法解析!");
			}

			HSSFSheet sheet = workBook.getSheetAt(0);
			if (sheet == null) {
				throw new SysException("第一个sheet没有数据！");
			}
			String beginRowIndex = StringUtil.getString(map.get("beginRowIndex"));
			int rowIndex = 0;
			if(!StringUtils.isEmpty(beginRowIndex)){
				rowIndex = Integer.parseInt(beginRowIndex);
			}
			// titles
			HSSFRow title = sheet.getRow(rowIndex);
			for (int cellNum = 0; cellNum <= title.getLastCellNum(); cellNum++) {
				HSSFCell cell = title.getCell(cellNum);
				if (cell == null) {
					break;
				}
				Object t = getValue(cell);
				listTitle.put(cellNum, t==null?null:t.toString());
			}
			int cellSize = listTitle.size();
			// 循环行Row
			for (int rowNum = 1 + rowIndex; rowNum <= sheet.getLastRowNum(); rowNum++) {
				HSSFRow row = sheet.getRow(rowNum);
				if (row == null) {
					continue;
				}
				Map<String,Object> mapRows = new HashMap<String, Object>();
				// 循环列Cell
				ArrayList<String> arrCell = new ArrayList<String>();
				for (int cellNum = 0; cellNum <= cellSize; cellNum++) {
					HSSFCell cell = row.getCell(cellNum);
					if (cell == null) {
						arrCell.add(null);
						continue;
					}
					String val = map.get(listTitle.get(cellNum));
					if(val != null){
						mapRows.put(val, getValue(cell));
					}
		
				}
				mapList.add(mapRows);
			}
		} catch (Exception e) {
			LogerUtil.error(ImportExcleUtil.class, e, "Parse Filee Error");
			throw new SysException("Parse Filee Error:"+ e.getMessage());
		}finally{
			if(inputS != null){
				IOUtils.closeQuietly(inputS);
			}
		}
		//System.out.println(mapList);
		return mapList;
	}

	/****
	 * 解析 XLS文件
	 * @param inputS 
	 * @param map 表头对就原字段名
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> xlsParse(String[] keys,InputStream inputS){
		Map<Integer,String> listTitle = new HashMap<Integer,String>();
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		try {
			HSSFWorkbook workBook = null;
			try {
				workBook = new HSSFWorkbook(inputS);
			} catch (Exception ex) {
				LogerUtil.error(ImportExcleUtil.class, ex, "Parse Filee Error");
				throw new SysException("文件无法解析!");
			}

			HSSFSheet sheet = workBook.getSheetAt(0);
			if (sheet == null) {
				throw new SysException("第一个sheet没有数据！");
			}
			// titles
			HSSFRow title = sheet.getRow(0);
			for (int cellNum = 0; cellNum <= title.getLastCellNum(); cellNum++) {
				HSSFCell cell = title.getCell(cellNum);
				if (cell == null) {
					break;
				}
				Object t = getValue(cell);
				listTitle.put(cellNum, t==null?null:t.toString());
			}
			int cellSize = listTitle.size();
			// 循环行Row
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				HSSFRow row = sheet.getRow(rowNum);
				if (row == null) {
					continue;
				}
				Map<String,Object> mapRows = new HashMap<String, Object>();
				// 循环列Cell
				ArrayList<String> arrCell = new ArrayList<String>();
				for (int cellNum = 0; cellNum <= cellSize; cellNum++) {
					HSSFCell cell = row.getCell(cellNum);
					if (cell == null) {
						arrCell.add(null);
						continue;
					}
					if(!StringUtils.isEmpty(getValue(cell))){
				      mapRows.put(keys[cellNum], getValue(cell));
					}
				}
				if(!mapRows.isEmpty()){
				   mapList.add(mapRows);
				}
			}
		} catch (Exception e) {
			LogerUtil.error(ImportExcleUtil.class, e, "Parse Filee Error");
			throw new SysException("Parse Filee Error:"+ e.getMessage());
		}finally{
			if(inputS != null){
				IOUtils.closeQuietly(inputS);
			}
		}
		return mapList;
	}

	
	
	/****
	 * 解析 XLSX文件
	 * @param inputS 
	 * @param map 表头对就原字段名
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> xlsxParse(InputStream inputS,Map<String, String>map)throws Exception{
		Map<Integer,String> listTitle = new HashMap<Integer,String>();
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		try {
			XSSFWorkbook workBook = null;
			try {
				workBook = new XSSFWorkbook(inputS);
			} catch (Throwable ex) {
				LogerUtil.error(ImportExcleUtil.class, ex, "Parse Filee Error");
				throw new SysException("文件无法解析!");
			}

			XSSFSheet sheet = workBook.getSheetAt(0);
			if (sheet == null) {
				throw new Exception("第一个sheet没有数据！");
			}
			String beginRowIndex = StringUtil.getString(map.get("beginRowIndex"));
			int rowIndex = 0;
			if(!StringUtils.isEmpty(beginRowIndex)){
				rowIndex = Integer.parseInt(beginRowIndex);
			}
			// titles
			XSSFRow title = sheet.getRow(rowIndex);
			for (int cellNum = 0; cellNum <= title.getLastCellNum(); cellNum++) {
				XSSFCell cell = title.getCell(cellNum);
				if (cell == null) {
					break;
				}
				Object t = getValue(cell);
				listTitle.put(cellNum, t==null?null:t.toString());
			}
			int cellSize = listTitle.size();
			// 循环行Row
			for (int rowNum = 1+rowIndex; rowNum <= sheet.getLastRowNum(); rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				if (row == null) {
					continue;
				}
				Map<String,Object> mapRows = new HashMap<String, Object>();
				// 循环列Cell
				List<String> arrCell = new ArrayList<String>();
				for (int cellNum = 0; cellNum < cellSize; cellNum++) {
					XSSFCell cell = row.getCell(cellNum);
					if (cell == null) {
						arrCell.add(null);
						continue;
					}
					String val = map.get(listTitle.get(cellNum));
					if(val != null){
						mapRows.put(val, getValue(cell));
					}
		
				}
				mapList.add(mapRows);
			}
		} catch (Exception e) {
			LogerUtil.error(ImportExcleUtil.class, e, "Parse File Error");
			throw new SysException("Parse Filee Error:"+ e.getMessage());
		}finally{
			if(inputS!=null){
				IOUtils.closeQuietly(inputS);
			}
		}
		return mapList;
	}
	
	/****
	 * 解析 XLSX文件
	 * @param inputS 
	 * @param map 表头对就原字段名
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> xlsxParse(String[] keys,InputStream inputS)throws Exception{
		Map<Integer,String> listTitle = new HashMap<Integer,String>();
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		try {
			XSSFWorkbook workBook = null;
			try {
				workBook = new XSSFWorkbook(inputS);
			} catch (Throwable ex) {
				LogerUtil.error(ImportExcleUtil.class, ex, "Parse Filee Error");
				throw new SysException("文件无法解析!");
			}

			XSSFSheet sheet = workBook.getSheetAt(0);
			if (sheet == null) {
				throw new Exception("第一个sheet没有数据！");
			}

			// titles
			XSSFRow title = sheet.getRow(0);
			for (int cellNum = 0; cellNum <= title.getLastCellNum(); cellNum++) {
				XSSFCell cell = title.getCell(cellNum);
				if (cell == null) {
					break;
				}
				Object t = getValue(cell);
				listTitle.put(cellNum, t==null?null:t.toString());
			}
			int cellSize = listTitle.size();
			// 循环行Row
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				if (row == null) {
					continue;
				}
				Map<String,Object> mapRows = new HashMap<String, Object>();
				// 循环列Cell
				List<String> arrCell = new ArrayList<String>();
				for (int cellNum = 0; cellNum < cellSize; cellNum++) {
					XSSFCell cell = row.getCell(cellNum);
					if (cell == null || cell.getRawValue() ==null) {
						arrCell.add(null);
						continue;
					}
					mapRows.put(keys[cellNum], getValue(cell));
				}
				if(!mapRows.isEmpty()){
					mapList.add(mapRows);
				}
			}
		} catch (Exception e) {
			LogerUtil.error(ImportExcleUtil.class, e, "Parse File Error");
			throw new SysException("Parse Filee Error:"+ e.getMessage());
		}finally{
			if(inputS!=null){
				IOUtils.closeQuietly(inputS);
			}
		}
		return mapList;
	}

	
	/**
	 * 获取XLSX文件单元格值 
	 * @param cell
	 * @return
	 */
	private static Object getValue(XSSFCell cell) {
		int cellType = cell.getCellType();
		if (cellType == XSSFCell.CELL_TYPE_BOOLEAN) {
			String v = cell.getStringCellValue();
			return v.trim();
		} else if(XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType()){
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				if (date != null) {
					return new SimpleDateFormat("yyyy-MM-dd").format(date);
				} 
				return date;
			} else if(XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType()) {
				BigDecimal db = new BigDecimal(cell.getNumericCellValue());
				String StrDecimal = db.toPlainString();
				return String.valueOf(StrDecimal).trim();
			}else {
				return String.valueOf(cell.getStringCellValue()).trim();
			}
		}else{
			int rowIndex = cell.getRowIndex() + 1;
			int cellIndex = cell.getColumnIndex() + 1;
			String v = null;
			try {
				if(XSSFCell.CELL_TYPE_FORMULA == cell.getCellType()){
					v = String.valueOf(cell.getNumericCellValue());
				}else{
					v = cell.getStringCellValue();
				}
			} catch (Exception e) {
				LogerUtil.error(ImportExcleUtil.class,"第" + rowIndex + "行，第" + cellIndex + "列，数据格式有误！");
			}
			return v == null ? null : v.trim();
		}
	}

	/**
	 * xls格式单元格值
	 * @param cell
	 * @return
	 */
	private static Object getValue(HSSFCell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue()).trim();
		} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				if (date != null) {
					return  new SimpleDateFormat("yyyy-MM-dd").format(date);
				} 
				return date;
			} else if(HSSFCell.CELL_TYPE_NUMERIC == cell.getCellType()) {
				BigDecimal db = new BigDecimal(cell.getNumericCellValue());
				String StrDecimal = db.toPlainString();
				return String.valueOf(StrDecimal).trim();
			}else {
				return String.valueOf(cell.getStringCellValue()).trim();
			}
		} else {
			int rowIndex = cell.getRowIndex() + 1;
			int cellIndex = cell.getColumnIndex() + 1;
			String v = null;
			try {
				if(XSSFCell.CELL_TYPE_FORMULA == cell.getCellType()){
					v = String.valueOf(cell.getNumericCellValue());
				}else{
					v = cell.getStringCellValue();
				}
			} catch (Exception e) {
				LogerUtil.error(ImportExcleUtil.class,"第" + rowIndex + "行，第" + cellIndex + "列，数据格式有误！");
			}
			return v == null ? null : v.trim();
		}
	}
	public static String rightTrim(String str) {
		if (str == null) {
			return "";
		}
		int length = str.length();
		for (int i = length - 1; i >= 0; i--) {
			if (str.charAt(i) != 0x20) {
				break;
			}
			length--;
		}
		return str.substring(0, length);
	}
	
	
}
