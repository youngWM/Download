package com.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库帮助类
 * 多线程操作数据库时注意使用【单例模式】
 * 1、构造方法定为private
 * 2、定义该类的一个静态对象用以应用
 * 3、通过getInstance（）方法返回该类对象，
 *    使该方法无论调用多少次，该类都是唯一。
 */
public class DBHelper extends SQLiteOpenHelper{

	private AtomicInteger mOpenCounter = new AtomicInteger();
	private SQLiteDatabase mDatabase;
	/**
	 * 数据库名称
	 */
	private static final String DB_NAME = "download.db" ;
	/**
	 * 数据库名称
	 */
	public static final String TABLE_NAME = "thread_info" ;
	/**
	 * 文件下载线程id
	 */
	public static final String THREAD_ID = "thread_id" ;
	/**
	 * 下载文件的url文件
	 */
	public static final String URL = "url" ;
	/**
	 * 文件下载的起始位置（字节长度）
	 */
	public static final String START = "start" ;
	/**
	 * 文件下载的结束位置（字节长度）
	 */
	public static final String END = "end" ;
	/**
	 * 文件已下载的字节长度
	 */
	public static final String FINISHED = "finished" ;
	/**
	 * 文件的md5
	 */
	public static final String MD5 = "md5" ;
	/**
	 * 文件是否完成下载标识
	 */
	public static final String OVER = "over" ;
	/**
	 * 文件是成下载时间
	 */
	public static final String OVER_TIME = "over_time" ;
	/**
	 * 数据库帮助类的静态对象引用
	 */
	private static DBHelper sHelper = null ;
	/**
	 * 数据库版本
	 */
	private static final int VERSION = 1;
	/**
	 * sql创建保存线程信息表命令句
	 */
	private static final String SQL_CREATE =
			"create table " +TABLE_NAME +" (_id integer primary key autoincrement,"
					+ THREAD_ID +" integer, "
					+ URL +" text, "
					+ START +" integer, "
					+ END +" integer,"
					+ FINISHED +" integer,"
					+ MD5 +" text, "
					+ OVER +" text, "
					+OVER_TIME+ " text )";
	/**
	 * 删除表命令句
	 */
	private static final String SQL_DROP = "drop table if exists "+TABLE_NAME;


	private DBHelper(Context context) {  		// 将public改为private，
		super(context, DB_NAME, null, VERSION);	// 防止在其它地方被new出来，保证db的单例，防止数据库被锁定
	}


	/**
	 * 获得类对象sHelper
	 */
	public static DBHelper getInstance(Context context){   // 单例模式，DBHelper只会被实例化一次
		if (sHelper == null ) {                            // 静态方法访问数据库，无论创建多少个数据库访问对象，
			sHelper = new DBHelper(context);			   // 里面的Helper只有一个，保证程序中只有一个DBHelper对数据库进行访问
		}
		return sHelper ;
	}


	public synchronized SQLiteDatabase openDatabase() {
		if(mOpenCounter.incrementAndGet() == 1) {
			// Opening new database
			mDatabase = sHelper.getWritableDatabase();
		}
		return mDatabase;
	}


	public synchronized void closeDatabase() {
		if(mOpenCounter.decrementAndGet() == 0) {
			// Closing database
			mDatabase.close();

		}
	}
	/**
	 * 创建表
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);  //创建表
	}


	/**
	 * 更新数据库表
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DROP);
		db.execSQL(SQL_CREATE);
	}


}
