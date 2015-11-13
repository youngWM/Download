package com.download.db;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.download.entities.DownloadConfig;
import com.download.entities.FileInfo;
import com.download.entities.ThreadInfo;
import com.download.tools.URLTools;
/**
 * 【数据库访问接口的实现】
 */
public class ThreadDAOIImpl implements ThreadDAO{

	/**
	 * 数据库帮助类
	 */
	private DBHelper mDBHelper = null ; // 将DBHelper用private修饰


	public ThreadDAOIImpl(Context context) {
		mDBHelper = DBHelper.getInstance(context);
	}


	/**
	 * 插入文件下载信息】
	 * 多线程数据库的增、删、改（更新）方法用synchronized修饰，以保证线程安全：
	 * 保证同一时间段不会有多个（只有一个）线程对数据库进行增删改，需等待线程执
	 * 行完后再开启线程执行下一个功能；而查询因为不用操作数据库，不会导致 数据库
	 * 死锁  ，所以不用
	 * @see com.download.db.ThreadDAO#insertThread(com.download.entities.ThreadInfo)
	 */
	@Override
	public synchronized void insertThread(ThreadInfo threadInfo ){
		//---------------
		SQLiteDatabase db = mDBHelper.getWritableDatabase();  // 实例化数据库，设置为【读写】模式
		db.execSQL( "insert into " +DBHelper.TABLE_NAME
						+ " ( "+DBHelper.THREAD_ID +","
						+DBHelper.URL       +","
						+DBHelper.START     +","
						+DBHelper.END       +","
						+DBHelper.FINISHED  +","
						+DBHelper.MD5       +","
						+DBHelper.OVER      +","
						+DBHelper.OVER_TIME +")"
						+ " values(?,?,?,?,?,?,?,?)",
				new Object[] {threadInfo.getId(),      threadInfo.getUrl(),
						threadInfo.getStart(),   threadInfo.getEnd(),
						threadInfo.getFinished(),threadInfo.getMd5(),
						threadInfo.getOver()    ,threadInfo.getOvertime()   } // 插入数据
		);
		db.close();  // 关闭数据库
	}

	/**
	 * 【删除文件下载信息】
	 * 多线程数据库的增、删、改（更新）方法用synchronized修饰，以保证线程安全：
	 * 保证同一时间段不会有多个（只有一个）线程对数据库进行增删改，需等待线程执
	 * 行完后再开启线程执行下一个功能；而查询因为不用操作数据库，不会导致 数据库
	 * 死锁  ，所以不用
	 */
	@Override
	public synchronized void deleteThread(String url) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		db.execSQL( "delete from thread_info where url = ?",
				new Object[] {url});
		db.close();
	}


	/**
	 * 【更新下载文件下载进度】
	 * 多线程数据库的增、删、改（更新）方法用synchronized修饰，以保证线程安全：
	 * 保证同一时间段不会有多个（只有一个）线程对数据库进行增删改，需等待线程执
	 * 行完后再开启线程执行下一个功能；而查询因为不用操作数据库，不会导致 数据库
	 * 死锁  ，所以不用
	 * @see com.download.db.ThreadDAO#updateThread(java.lang.String, int, int, java.lang.String)
	 */
	@Override
	public synchronized void updateThread(String url, int thread_id, int finished, String md5, String over, String over_time) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		db.execSQL( "update " +DBHelper.TABLE_NAME + " set "
						+DBHelper.FINISHED+" = ?where " +DBHelper.URL       +" =? and "
						+DBHelper.THREAD_ID +" =?",
				new Object[] {finished, url, thread_id});
		db.execSQL( "update " +DBHelper.TABLE_NAME + " set "
						+DBHelper.OVER+" = ?where " +DBHelper.URL       +" =? and "
						+DBHelper.THREAD_ID +" =?",
				new Object[] {over, url, thread_id});
		db.execSQL( "update " +DBHelper.TABLE_NAME + " set "
						+DBHelper.OVER_TIME+" = ?where " +DBHelper.URL  +" =? and "
						+DBHelper.THREAD_ID +" =?",
				new Object[] {over_time, url, thread_id});
		db.close();
	}


	/**
	 * 获取文件的文件下载信息
	 * @return List<ThreadInfo> 包含下载线程信息的list集
	 * @see com.download.db.ThreadDAO#getThreads(java.lang.String)
	 */
	@Override
	public List<ThreadInfo> getThreads(String url) {
		//-------------
		SQLiteDatabase db = mDBHelper.getReadableDatabase();  //注意，此处用【只读】模式
		List<ThreadInfo> list = new ArrayList<ThreadInfo>();
		Cursor cursor = db.rawQuery("select * from " +DBHelper.TABLE_NAME
				+ " where " +DBHelper.URL +" =?", new String[]{url} );
		while (cursor.moveToNext()) {
			ThreadInfo threadInfo = new ThreadInfo();
			threadInfo.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.THREAD_ID)));
			threadInfo.setUrl(cursor.getString(cursor.getColumnIndex(DBHelper.URL)));
			threadInfo.setStart(cursor.getInt(cursor.getColumnIndex(DBHelper.START)));
			threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex(DBHelper.END)));
			threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex(DBHelper.FINISHED)));
			threadInfo.setMd5(cursor.getString(cursor.getColumnIndex(DBHelper.MD5)));
			threadInfo.setOver(cursor.getString(cursor.getColumnIndex(DBHelper.OVER)));
			list.add(threadInfo);
		}
		cursor.close();
		db.close();
		return list;
	}

	/**
	 * 获取数据库中所有下载文件的下载信息
	 * @return List<ThreadInfo> 包含下载线程信息的list集
	 */
	@Override
	public List<FileInfo> getDBFileInfoList() {

		URLTools urlTools = new URLTools();
		List<FileInfo> list = new ArrayList<FileInfo>();
		SQLiteDatabase db = mDBHelper.getReadableDatabase();  //注意，此处用【只读】模式
		//查询数据库文件下载信息
		Cursor cursor = db.rawQuery("select * from " +DBHelper.TABLE_NAME, null );
		while( cursor.moveToNext() ){
			FileInfo fileInfo = new FileInfo();
			//-----------
			if( fileInfo.getId()==0 ){
				long finish = 0;  //文件已下载的字节数
				fileInfo.setOver( cursor.getString(cursor.getColumnIndex(DBHelper.OVER)) );
				fileInfo.setOvertime( cursor.getString(cursor.getColumnIndex(DBHelper.OVER_TIME)) );
				fileInfo.setMd5( cursor.getString(cursor.getColumnIndex(DBHelper.MD5)) );
				fileInfo.setUrl( cursor.getString(cursor.getColumnIndex(DBHelper.URL)) );
				String fileName = urlTools.getURLFileName( fileInfo.getUrl(), "/" );
				fileInfo.setFileName( fileName );
				fileInfo.setIsDownload("false");
				//获取百分比下载进度
				for (int i = 0; i < DownloadConfig.DONWNLOAD_THREAD_NUM; i++) {
					finish += cursor.getInt( cursor.getColumnIndex(DBHelper.FINISHED) );
					if ( i < DownloadConfig.DONWNLOAD_THREAD_NUM -1 ){
						cursor.moveToPosition( cursor.getPosition() +1 );
					}
				}
				long finished = finish*100/( cursor.getInt(cursor.getColumnIndex(DBHelper.END)) );
				fileInfo.setFinished( (int)finished);
				list.add(fileInfo);
			}
		}
		//---------
		cursor.close();
		db.close();
		return list;
	}


	/**
	 * 文件下载信息是否已存在
	 * @return 当存在下载信息返回true；否则返回false
	 */
	@Override
	public boolean isDownloadOver(String url) {

		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from " +DBHelper.TABLE_NAME
						+" where " +DBHelper.URL  +" =? and " +DBHelper.THREAD_ID  +" = ?",
				new String[]{url, 1+"" } );
		boolean over = false;
		if (cursor.moveToNext()) {
			String overStr =   cursor.getString(cursor.getColumnIndex(DBHelper.OVER)) ;
			try {
				String name =  URLDecoder.decode(cursor.getString(cursor.getColumnIndex(DBHelper.URL)), "UTF-8")  ;
				if (overStr.equals("true")) {
					over = true ;
					Log.i("db", name+"文件是否下载完成："+ over  );
				}else {
					over = false ;
					Log.i("db",name+ "文件是否下载完成："+ over  );
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		db.close();

		return over;
	}

	/**
	 * 文件下载信息是否已存在
	 * @return 当存在下载信息返回true；否则返回false
	 */
	@Override
	public boolean isExists(String url, int thread_id) {

		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
				new String[]{url, 0+ ""} );
		boolean exists = cursor.moveToNext();
		cursor.close();
		db.close();

		return exists;
	}

}
