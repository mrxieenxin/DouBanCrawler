package com.xex;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.xex.entity.BookInfo;

public class ExcelUtil<T> {
	public void exportExcel(String[] headers, List<T> dataset, String fileName, FileOutputStream file) {
		// 声明一个工作薄
		XSSFWorkbook workbook = new XSSFWorkbook();
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet(fileName);
		// 设置表格默认列宽度为15个字节
		sheet.setDefaultColumnWidth((short) 20);
		// 产生表格标题行
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		style.setFont(font);
		XSSFRow row = sheet.createRow(0);
		for (short i = 0; i < headers.length; i++) {
			XSSFCell cell = row.createCell(i);
			XSSFRichTextString text = new XSSFRichTextString(headers[i]);
			cell.setCellValue(text);
			cell.setCellStyle(style);
			// 单元格样式
		}
		try {
			// 遍历集合数据，产生数据行
			
			Iterator<T> it = dataset.iterator();
			int index = 0;
			int count=1;
			while (it.hasNext()) {
				index++;
				row = sheet.createRow(index);
				T t = (T) it.next();
				// 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
				Field[] fields = t.getClass().getDeclaredFields();
				for (short i = 0; i < headers.length; i++) {
					XSSFCell cell = row.createCell(i);
					if(i==0){
						//设置序号
						cell.setCellValue(count++);
					}else{
						Field field = fields[i];
						String fieldName = field.getName();
						String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
						Class tCls = t.getClass();
						Method getMethod = tCls.getMethod(getMethodName, new Class[] {});
						Object value = getMethod.invoke(t, new Object[] {});
						// 判断值的类型后进行强制类型转换
						String textValue = null;
						// 其它数据类型都当作字符串简单处理
						if (value != null && value != "") {
							textValue = value.toString();
						}
						if (textValue != null) {
							XSSFRichTextString richString = new XSSFRichTextString(textValue);
							cell.setCellValue(richString);
						}
					}
				}
			}
			getExportedFile(workbook, fileName, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 方法说明: 指定路径下生成EXCEL文件
	 * 
	 * @return
	 */
	public void getExportedFile(XSSFWorkbook workbook, String name, FileOutputStream file) throws Exception {
		BufferedOutputStream fos = null;
		try {
			fos = new BufferedOutputStream(file);
			workbook.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			file.close();
			if (fos != null) {
				fos.close();
			}
		}
	}
}
