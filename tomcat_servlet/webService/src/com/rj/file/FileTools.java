package com.rj.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.rj.tools.UnixTime;

public class FileTools {

	public FileTools() {
	}
	
	/**************************************************
	***************************************************/
	
	/**
	 * 在根目录下新建temp文件夹
	 * @param location 检测文件保存位置是否存在，没有则创建
	 */
	public void builtFile(String location){
		String Save_Location;
		Save_Location= location;
		if (!(new File( Save_Location).isDirectory() ) ){ //如果文件夹不存在，则新建
			 File myFilePath = null;
			 myFilePath = new File(Save_Location);
			 myFilePath.mkdir(); 
		}
	}
	
	/**
	 * 创建文本
	 * @param builtNum  新建文本的个数
	 * @param location  文本保存位置
	 */
	public void builtTxt(int builtNum, String location ){
		PrintWriter out = null;
		String unixtime = null;
		for (int i = 0; i < builtNum; i++) {
			try {
				unixtime = UnixTime.toUnixTime();
			
				String filename = location+"//文件"+unixtime+".txt";
				if (!(new File(filename).isDirectory()) ){
					File f = new File(filename) ;
				try{
					 out = new PrintWriter(new FileWriter(f) ) ;
					 //----------------
					 // 由 FileWriter 实例化，则向文件中写入内容
					 out. print ("该txt文件名为：file"+unixtime+".txt"+"\r\n");
					 out.close();
					}catch (IOException e)
					{
						e.printStackTrace();
					}
				 }
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

 
}
