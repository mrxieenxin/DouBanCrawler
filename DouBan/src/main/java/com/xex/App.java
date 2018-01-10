package com.xex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xex.entity.BookInfo;

public class App{
	//这四个变量是为了防止key相同去重复问题，设置成全局成员变量
	int i=1;
	int j=1;
	int k=1;
	int l=1;
	/**
	 * 根据种类抓取信息，返回map
	 * @param sort
	 * @return
	 * @throws Exception
	 */
	public  Map<Integer, BookInfo> parseUrl(String sort) throws Exception{
		 // 确定目标地址 URL 统一资源定位符
        String url="https://book.douban.com/";
        // 2 解析 html ：  https：//jsoup.org
        try {
            //模拟浏览器，防止被DouBan软禁
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31").get();
            //.class 选择器
            //标签选择器
            Elements els = doc.select("a");
            System.out.println(els.size());
            // 创建一个线程池
            //.class 选择器
            ExecutorService pool = Executors.newCachedThreadPool();
            pool = Executors.newFixedThreadPool(9);
//            pool = Executors.newSingleThreadExecutor();
            for(Element e : els) {
            	//创建一个list
            	//找到class为tag的标签，拿到文本信息
                String tag = e.attr("class");
                //判断是不是tag类，如果是这个class就证明这是类别
                if("tag".equals(tag.trim())){
                	//看这个节点是否有文本
                	if(e.hasText()){
                		String text = e.text();
                		//找到目标类别
                		if(sort.equals(text)){
                			//拿到网址信息
                			String href = e.attr("href");
                			href = href.substring(1);
                			//进入这几个网页抓取信息
                			String newHref=url+href;
                			System.out.println(newHref);
                			//输入网址
                			Map<Integer, BookInfo> map = getData(newHref);
                			//一页20条，下一页
                			for(int k=1;k<=1000;k++){
                				//每一页下一页的分页单位不同,这里循环抓取下一页数据，
                				String nextUrl="tag/"+sort+"?start="+k*20+"&amp;type=T";
                				String newUrl=url+nextUrl;
                				//循环抓取返回的map
                				Map<Integer, BookInfo> map2 = getData(newUrl);
                				//封装到一个map中
                				map.putAll(map2);
                				//防止豆瓣封ip少抓一点
                				if(map.values().size()>=500){
                					//到现在位置所有的map已经装完；
                        			return map;
                				}
                			}
                		}
                	}
                }
            }
            pool.shutdown();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return null;
	}
	
	/**
	 * 
	 * @param newHref网址
	 * @param text 类别
	 * @throws Exception
	 */
	public  Map<Integer,BookInfo> getData(String newHref) throws Exception{
		//打开这三类的网页
		 Document newDoc = Jsoup.connect(newHref).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31").get();
		 //获取元素
		 Elements ees = newDoc.select("a");
		 Map<Integer,BookInfo> map = new HashMap<Integer,BookInfo>();
		 for (Element ee : ees) {
			 BookInfo book = new BookInfo();
			 //标题
			 String title = ee.attr("title");
			 //得到标题
			 if(!"".equals(title.trim())){
				 book.setBookName(title);
				 map.put(i, book);
				 i++;
			 }
		}
		 //得到评价
		Elements commentEle = newDoc.select(".rating_nums");
		for (Element comE : commentEle) {
			if(comE.hasText()){
				//拿到评价分数
				BookInfo book = map.get(j);
				if(book!=null){
					//设置book评价分数
					book.setCommentNum(comE.text());
				}
				j++;
			}
		}
		//评论人数
		Elements numEle = newDoc.select(".pl");
		int count=1;
		for (Element comE : numEle) {
			if(comE.hasText()&&count<=20){
				BookInfo book = map.get(k);
				//20之后就是没用的数据了
				//拿到评价人数
				if(book!=null){
					book.setCommentCount(getNum(comE.text()));
				}
				count++;
				k++;
			}
		}
		//作者，出版社，日期，价格
		Elements strEle = newDoc.select(".pub");
		for (Element comE : strEle) {
			if(comE.hasText()){
				BookInfo book = map.get(l);
				//拿到这四个信息
				String str = comE.text();
				String[] strings = str.split("/");
				if(strings.length==5){
					//外国的加翻译多一个人
					book.setAuthor(strings[0]+"/"+strings[1]);
					book.setPress(strings[2]);
					book.setDate(strings[3]);
					book.setPrice(strings[4]);
				}else if(strings.length==4){
					//不是外国的就正常的4组
					book.setAuthor(strings[0]);
					book.setPress(strings[1]);
					book.setDate(strings[2]);
					book.setPrice(strings[3]);
				}
				l++;
			}
		}
		return map;
	}
	/**
	 * 根据评价人的字符串获取评价人数量
	 * @param string
	 * @return
	 */
	public static long getNum(String string){
		//创建StringBuffer对象
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<string.length();i++){
			char c = string.charAt(i);
			if(c<='9'&&c>='0'){
				//是数字就进行拼接
				sb.append(c);
			}
		}
		//转换成数字
		if(!"".equals(sb.toString().trim())){
			return Long.parseLong(sb.toString()); 
		}
		return 0;
	}
}
