package com.download.db;

import com.download.entities.FileInfo;
import com.download.entities.ThreadInfo;

import java.util.List;

/**
 * 【数据访问接口】
 * DAO：data access object 数据访问对象，DAO一定是和数据库的每张表一一对应;
 * 是一种应用程序表承接口（api）,可以访问其它的结构化查询语言（SQL）数据库；
 * J2EE用DAO设计模块，把底层数据访问和高层的商务逻辑分开；典型的DAO有以下几个组件：
 * 1、一个DAO工厂类
 * 2、一个DAO接口
 * 3、一个实现DAO接口的具体类
 * 4、数据传递对象
 */
public interface ThreadDAO {


	/**
	 * 插入文件下载信息
	 * @param threadInfo  下载线程的信息
	 */
	public void insertThread(ThreadInfo threadInfo);



	/**
	 * 删除文件下载信息
	 */
	public void deleteThread(String url );



	/**
	 * 更新下载文件下载进度
	 * @param url  下载线程的URL
	 * @param thread_id  下载线程的id
	 * @param finished   下载线程的文件已下载字节数
	 * @param md5   下载文件的md5
	 * @param over   文件下载完成标识
	 * @param over_time   文件下载完成时间
	 */
	public void  updateThread(String url, int thread_id, int finished, String md5, String over, String over_time) ;


	/**
	 * 查询文件的文件下载线程信息
	 * @param url  下载线程的URL
	 * @return  List<ThreadInfo> 线程信息集
	 */
	public List<ThreadInfo> getThreads(String url) ;



	/**
	 * 获取所有文件的文件下载信息
	 * @return List<ThreadInfo> 包含下载线程信息的list集
	 */
	public List<FileInfo> getDBFileInfoList();


	/**
	 * 文件下载信息是否已结束下载
	 * @return 当存在下载信息返回true；否则返回false
	 */
	boolean isDownloadOver(String url);



}
