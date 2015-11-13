package com.rj.tools;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONStringer;

public class JsonData {

	public JsonData() {

	}
	
	public String setJsonData(File[] dowmloadList) throws JSONException, IOException{
		MD5Util md5Util = new MD5Util();
		JSONStringer stringer = new JSONStringer();
		stringer.object();
			//------------------------
			stringer.key("URL");
			stringer.object();
			for(int i=0;i<(dowmloadList.length);i++){
				stringer.key("file-" +i);
				URLName name = new URLName();
				String fileName = name.fileName(dowmloadList[i].getAbsolutePath(), "\\");
				String expandName = name.expandName(dowmloadList[i].getAbsolutePath());
				stringer.value("\\" +URLEncoder.encode(fileName, "UTF-8") +"." +expandName );
			}
			stringer.endObject();
			//-------------------------
			stringer.key("MD5");
			stringer.object();
			System.out.println("dowmloadList.length");
			for(int i=0;i<(dowmloadList.length);i++){
				stringer.key("file-" +i);
				File file = new File(dowmloadList[i].getAbsolutePath());
				String md5 = md5Util.getFileMD5String(file);
				System.out.println("file:"+file.getAbsolutePath()+" md5:"+md5);
				stringer.value(md5);
			}
			stringer.endObject();
			//---------------------		
		stringer.endObject();
		return stringer.toString();
	}
}
