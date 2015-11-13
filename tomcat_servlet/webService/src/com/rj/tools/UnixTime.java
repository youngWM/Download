package com.rj.tools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UnixTime {
	
	public static void main(String[] args) {
		String unix = "1430755200" ;
		String data = toLocalTime(unix) ;
		System.out.println(data);
		
		System.out.println(toUnixTime("2015-05-05"));
	}
	
	//��unixʱ���ת��Ϊ����ʱ��
	public static String toLocalTime(String unix) {
	    long timestamp = Long.parseLong(unix) * 1000;
	    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
	    return date;
	}
	//������ʱ��ת��Ϊunixʱ���
	public static long toUnixTime(String local){
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    long unix = 0 ;
	    try {
	        unix = (df.parse(local).getTime() )/1000;
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    return unix;
	}
	//������ʱ��ת��Ϊunixʱ���
	public static String toUnixTime() throws InterruptedException{
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("ddHHmmssSSS");//�������ڸ�ʽ
		String time = format.format(date);	//��ȡ��ǰʱ��
		
		return time;
	}

}
