package com.download.servers;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.download.entities.DownloadConfig;
import com.download.entities.FileInfo;
import com.download.entities.IntentAction;
import com.download.entities.KeyName;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

//import android.util.Log;

/**
 * 用于文件下载的service
 */
public class DownloadService extends Service {

	/**
	 * 定义消息处理handler的标志
	 */
	private static final int MSG_INIT = 0 ;

	/**
	 * 文件下载线程任务的集合
	 */
	private Map<Integer, DownloadTask> mTask = new LinkedHashMap<Integer, DownloadTask>();



	/**
	 * onStartCommand（）接收Activity中StartService发送的信息
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//-------------------
		// 获取Activity传进来的参数
		FileInfo fileInfo =(FileInfo) intent.getSerializableExtra( KeyName.FILEINFO_TAG );// 获取下载文件信息
		Log.i("intent", "DownloadService-47行- "+fileInfo.getFileName()+" intent=null: " + (intent==null) );
		Log.i("intent", "DownloadService-48行- "+fileInfo.getFileName()+" intent.getSerializableExtra(Config.FILEINFO_TAG):"
				+(intent.getSerializableExtra(KeyName.FILEINFO_TAG)==null) );
		// 开启多线程下载
		if ( IntentAction.ACTION_START.equals(intent.getAction()) ) {
			initThread mInitThread = new initThread(fileInfo);
			DownloadTask.sExecutorService.execute(mInitThread);// 通过线程池开启初始化线程
			// 暂停多线程下载
		}else if ( IntentAction.ACTION_PAUSE.equals(intent.getAction()) ) {
			DownloadTask tast = mTask.get(fileInfo.getId() ) ; // 从集合中取出下载任务
			if (tast != null) {
				tast.isPause = true ;
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}



	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	/**
	 * 初始化文件下载线程：创建本地下载位置，并开启下载任务
	 **/
	class initThread extends Thread{
		/**
		 * 下载的文件的所有属性信息
		 */
		private FileInfo mFileInfo = null ;

		/**
		 * 初始化文件下载线程：确保创建本地下载位置，并开启文件下载任务
		 * @param mFileInfo 下载的文件的所有属性信息
		 */
		public initThread(FileInfo mFileInfo) {
			this.mFileInfo = mFileInfo;
		}

		//----------------
		//开启开启下载任务
		Handler handler = new Handler(){
			@Override
			public void handleMessage(android.os.Message msg) {
				//-----------------
				switch (msg.what) {
					case MSG_INIT:
						FileInfo fileInfo = (FileInfo)  msg.obj ;
						// 启动下载任务
						DownloadTask task = new DownloadTask( DownloadService.this,
								fileInfo, DownloadConfig.DONWNLOAD_THREAD_NUM);
						task.download();
						//把下载任务添加到下载集合中
						mTask.put(fileInfo.getId(), task);  //将开启下载线程的id和实例添加到map中，在暂停时通过id获取实例，并令它暂停
						break;

					//-----------------
					default:
						break;
				}
			}
		};

		//----------------
		@Override
		public void run(){

			HttpURLConnection conn = null ;
			RandomAccessFile raf = null ;// 随机访问文件，可以在文件的随机写入，对应断点续传功能
			try {
				// 连接网络文件
				URL url = new URL(mFileInfo.getUrl() );
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);// 设置连接超时时间
				conn.setRequestMethod("GET");

				// 获取文件长度
				int length = -1;
				if (conn.getResponseCode()== HttpStatus.SC_OK ) {
					length = conn.getContentLength();
					Log.i("http", "DownloadService-138行 获取网络数据长度="+ length );
				}else{
					Log.i("http", "DownloadService-140行 http连接失败！");
				}
				if ( length <= 0 ){
					return ;
				}

				// 判断下载路径是否存在
				File dir = new File( DownloadConfig.DOWNLOAD_PATH );
				if ( !dir.exists() ){
					dir.mkdir();
				}

				// 在本地创建文件
				File file = new File( dir, mFileInfo.getFileName() );
				raf = new RandomAccessFile( file, "rwd" );

				// 设置文件长度
				raf.setLength( length );
				mFileInfo.setLength( length );
				handler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();  //将数据发回给handler

				//关闭连接
				if (raf!=null) {
					raf.close();
				}if (conn != null) {
					conn.disconnect();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
