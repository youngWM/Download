package com.download.tools;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


@SuppressLint("DefaultLocale") public class Tools {

    /**
     * 移除字符串list中重复的数据，并且不会改变数据顺序
     * @param list  字符串list
     * @return 无重复数据的字符串list
     */
    public static List<String> removeDuplicateWithOrder(List<String> list)
    {
        HashSet<String> hashSet = new HashSet<String>();
        List<String> newlist = new ArrayList<String>();
        for (Iterator iterator = list.iterator(); iterator.hasNext();)
        {
            String element = (String) iterator.next();
            if (hashSet.add(element))
            {
                newlist.add(element);
            }
        }
        list.clear();
        list.addAll(newlist);
        return list;
    }

    /**
     * 获取list中不相同的值
     */
//	public List<FileInfo>  getDifferent(List<FileInfo> finish, List<FileInfo> all){
//		List<String> url = 
//		for (FileInfo fileInfo : finish) {
//			
//		}
//		List<FileInfo>  diff = new ArrayList<FileInfo>();
//		for (FileInfo fileInfo : all) {
//			if ( !finish.contains(fileInfo) ){
//				diff.add(fileInfo);
//			}
//		}
//		return diff;
//	}

    /**
     * 对字符串进行Base64编码
     * @param content
     * @return Base64编码后的string
     */
    @SuppressWarnings("deprecation")
    public String base64(String content){
        try {
            content=Base64.encodeToString(content.getBytes("utf-8"), Base64.DEFAULT);//对字符串进行Base64编码
            content=URLEncoder.encode(content);	//对字符串进行URL编码
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }
}
