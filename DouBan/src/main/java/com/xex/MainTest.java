package com.xex;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.xex.entity.BookInfo;

public class MainTest implements Runnable {
	ExcelUtil<BookInfo> util = new ExcelUtil<BookInfo>();
	String headers[] = { "序号", "书名", "评分", "评价人数", "作者", "出版社", "出版日期", "价格" };
	private String sort;

	public MainTest(String sort) {
		this.sort = sort;
	}

	public static void main(String[] args) {

		try {
			//工厂设计模式获取excel
			MainTest test = new MainTest("互联网");
			Thread t1 = new Thread(test);
			MainTest test2 = new MainTest("编程");
			Thread t2 = new Thread(test2);
			MainTest test3 = new MainTest("算法");
			Thread t3 = new Thread(test3);
			//开启多线程抓取数据
			t1.start();
			t2.start();
			t3.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			App app = new App();
			Map<Integer, BookInfo> map;
			map = app.parseUrl(sort);
			ArrayList<BookInfo> list = new ArrayList<BookInfo>();
			Set<Entry<Integer,BookInfo>> set = map.entrySet();
			//遍历，小于评论人数多于1000的放进list
			for (Entry<Integer, BookInfo> entry : set) {
				//去除小于1000的
				if(entry.getValue().getCommentCount()>=1000){
					list.add(entry.getValue());
				}
				if(list.size()>=100){
					break;
				}
			}
			File file = new File("F:/"+sort+".xlsx");
			FileOutputStream outputStream = new FileOutputStream(file);
			util.exportExcel(headers, list, sort, outputStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
