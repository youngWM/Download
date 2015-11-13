package com.rj.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tomcat.jni.FileInfo;

import sun.rmi.runtime.Log;
	/**
	 * 获取文件的md5值
	 */
	public class MD5Util {
	
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
										  'a', 'b', 'c', 'd', 'e', 'f' };
	protected static MessageDigest messagedigest = null;
	static{
	   try{
		   	 messagedigest = MessageDigest.getInstance("MD5");
	   	  }catch(NoSuchAlgorithmException e){
	   		 e.printStackTrace();
	   	  }
	}
	
	/**
	 * 获取文件的md5值,用synchronized修饰使之同一时间只能执行一个
	 * @param file 文件位置
	*/
	public static synchronized String getFileMD5String( File file ) throws IOException {
	   FileInputStream in = new FileInputStream(file);
	   FileChannel ch = in.getChannel();
	   MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
	   messagedigest.update(byteBuffer); 
	   in.close();
	   return bufferToHex(messagedigest.digest());
	}
	
	private static String bufferToHex(byte bytes[]) {
	   return bufferToHex(bytes, 0, bytes.length);
	}
	
	private static String bufferToHex(byte bytes[], int m, int n) {
	   StringBuffer stringbuffer = new StringBuffer(2 * n);
	   int k = m + n;
	   for (int l = m; l < k; l++) {
	    appendHexPair(bytes[l], stringbuffer);
	   }
	   return stringbuffer.toString();
	}
	
	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
	   char c0 = hexDigits[(bt & 0xf0) >> 4];
	   char c1 = hexDigits[bt & 0xf];
	   stringbuffer.append(c0);
	   stringbuffer.append(c1);
	}
	
}