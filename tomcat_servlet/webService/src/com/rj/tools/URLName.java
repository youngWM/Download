package com.rj.tools;

public class URLName {

	public URLName() {
	}
	
	/**
	 * 获取完整http的URL中的含扩展名的文件名
	 * @param url 	    完整http的URL
	 * @param symbol  url中路径是正斜杠“/”或反斜杠“\”或者本地路径的"\\"
	*/
	public String getURLFileName(String url,String symbol){
		String fileName   = url.substring(url.lastIndexOf(symbol) + 1,url.lastIndexOf(".")); 
		String expandName = url.substring(url.lastIndexOf(".") + 1,url.length()).toLowerCase(); 
		
		return fileName+"."+expandName;
	}
	
	/**
	 * 获取完整http的URL中不含扩展名的文件名
	 * @param url 	    完整http的URL
	 * @param symbol  url中路径是正斜杠“/”或反斜杠“\”或者本地路径的"\\"
	*/
	public String fileName(String url,String symbol){
		String fileName   = url.substring(url.lastIndexOf(symbol) + 1,url.lastIndexOf(".")); 
		return fileName;
	}
	
	/**
	 * 获取完整http的URL中的文件名的扩展名
	 * @param url 	    完整http的URL
	 * @param symbol  url中路径是正斜杠“/”或反斜杠“\”或者本地路径的"\\"
	*/
	public String expandName(String url){
		String expandName = url.substring(url.lastIndexOf(".") + 1,url.length()).toLowerCase(); 
		return expandName;
	}
}
