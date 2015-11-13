<%@page import="com.rj.tools.JsonData"%>
<%@page import="com.rj.file.FileTools"%>
<%@page import="com.rj.tools.URLName"%>
<%@page import="org.apache.commons.codec.digest.DigestUtils"%>
<%@page import="java.util.Random"%>
<%@page import="com.rj.tools.UnixTime"%>
<%@page import="com.rj.tools.MD5Util"%>
<%@page import="org.json.JSONStringer"%>
<%@page import="com.sun.xml.internal.bind.v2.schemagen.xmlschema.List"%>
<jsp:directive.page import="org.json.*"/>
<%@ page 
	contentType="text/html; charset=utf-8" 
	language="java" 
	import="java.io.File"
	import="java.io.FileWriter"
	import="java.io.IOException"
	import="java.io.PrintWriter"
	import="java.net.InetAddress"
	import="sun.misc.BASE64Decoder" 
	import="java.net.URLEncoder"
	import="java.util.Date"
	import="java.text.SimpleDateFormat"
	import="java.text.ParseException"
	import="java.security.MessageDigest"
	import="java.net.UnknownHostException"	
	%>

<%
	
	String answer = "";
	 if( request.getParameter("content") != null ){
		 
		// 获取client请求信息
		String content = request.getParameter("content");// 获取client输入的信息
		content=content.replaceAll("%2B","+");	// 替换content中的加号，这是由于在进行URL编码时，将+号转换为%2B了
	 	BASE64Decoder decoder = new BASE64Decoder();
		content = new String(decoder.decodeBuffer(content),"utf-8");	//进行base64解码
		
		// 若请求为“请求下载”，则返回txt文件URL
		if(content.equals("请求下载")){
			
			// 创建文件夹和txt
			FileTools fileTools = new FileTools();
			String Save_Location = getServletContext().getRealPath("/")+"temp";
			fileTools.builtFile(Save_Location);
			fileTools.builtTxt(2, Save_Location);
			
			
			// 获取json数据
			File fdown = null;
			fdown = new File(Save_Location);
			File[] dowmloadList = fdown.listFiles();
			JsonData jsonData = new JsonData();
			answer = jsonData.setJsonData(dowmloadList);
		}
	 }

	
%>

<%=answer%>
