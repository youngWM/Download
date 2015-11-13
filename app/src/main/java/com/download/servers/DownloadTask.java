package com.download.servers;

import android.content.Context;
import android.content.Intent;

import com.download.db.ThreadDAO;
import com.download.db.ThreadDAOIImpl;
import com.download.entities.DownloadConfig;
import com.download.entities.FileInfo;
import com.download.entities.IntentAction;
import com.download.entities.KeyName;
import com.download.entities.ThreadInfo;
import com.download.tools.DateTools;
import com.download.tools.MD5Util;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下载任务类
 **/
public class DownloadTask {
	/**
	 * 上下文
	 */
	private Context  mContext = null ;
	/**
	 * 下载的文件的所有属性信息
	 */
	private FileInfo mFileInfo = null ;
	/**
	 * 数据（库）访问接口
	 */
	//------------------------
	private ThreadDAO mDAO = null;
	/**
	 * 已下载字节长度
	 */
	private int mFinishedLen  = 0;
	/**
	 * 下载暂停标志
	 */
	public  Boolean isPause = false;

	//------------------------
	/**
	 * 默认的下载线程数
	 */
	private int mThreadNum  = 0 ;
	/**
	 * 下载线程集合
	 */
	private List<DownloadThread> mThreadList = null ;
	/**
	 * 带缓存线程池，s开头表示用到static关键字 
	 */
	public static ExecutorService sExecutorService = Executors.newCachedThreadPool();




	/**
	 * 文件下载的线程任务类
	 * @param mContext  上下文
	 * @param mFileInfo 下载的文件的所有属性信息
	 * @param threadNum 文件分段下载线程数
	 */
	public DownloadTask(Context mContext, FileInfo mFileInfo, int threadNum) {
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadNum = threadNum ;
		mDAO = new ThreadDAOIImpl(mContext);
	}




	/**
	 * 开始下载文件
	 */
	public  void download() {
		List<ThreadInfo> threadInfoList = mDAO.getThreads(mFileInfo.getUrl());  // 读取数据库中文件下载的信息
		// 不存下载线程则创建
		if( threadInfoList.size()== 0 ){
			int length = mFileInfo.getLength()/mThreadNum ; // 获取单个线程下载长度
			for (int i = 0; i < mThreadNum; i++) {			// 创建线程信息
				ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),length*i, (i+1)*length-1, 0, mFileInfo.getMd5(),"false", "none");
				if (i==mThreadNum-1) {
					threadInfo.setEnd(mFileInfo.getLength());// 设置最后一个线程的下载长度
				}
				threadInfoList.add(threadInfo);	// 添加线程信息到集合
				mDAO.insertThread(threadInfo);  // 向数据库插入文件下载的信息
			}									// 放到run外面，比较不容易产生数据库的死锁   
		}

		// 启动多个线程进行下载
		mThreadList = new ArrayList<DownloadThread>();
		for (ThreadInfo info : threadInfoList) {
			DownloadThread downloadThread = new DownloadThread(info);
			if ( info.getFinished()<(info.getEnd()-info.getStart())&&  info.getId()==0  ||
					info.getFinished()<(info.getEnd()-info.getStart()+1)&&  info.getId()!=0	) {  //若未完成下载则下载
				DownloadTask.sExecutorService.execute(downloadThread);
				mThreadList.add(downloadThread);					   //添加线程到集合中
			}
		}
	}





	/**
	 * 判断所有线程是否执行完成
	 * @param threadInfo  下载文件的线程信息
	 */
	private synchronized  void checkAllThreadsFinished(ThreadInfo threadInfo){  // synchronized 同步方法，保证同一时间只有一个线程访问该方法

		boolean allFinished = true ;   //所有线程下载结束标识
		// 遍历线程集合，判断是否都下载完毕
		for (DownloadThread thread : mThreadList) {
			if (!thread.isFinished) {
				allFinished = false ;
				break ;
			}
		}
		// 所有线程下载结束：验证md5，相同则存储下载长度，否则清空；发送广播通知UI下载任务结束
		if (allFinished) {

			MD5Util md5Util = new MD5Util();
			DateTools dateTools = new DateTools();
			List<ThreadInfo> threadInfosList =mDAO.getThreads(threadInfo.getUrl());
			for (ThreadInfo info :threadInfosList) {
				if ( !md5Util.isMD5Equal(info) ) {  // md5验证失败，清空下载长度
					mDAO.updateThread(info.getUrl(), info.getId(), 0, info.getMd5(), "false" ,dateTools.getCurrentTime() );
				}else if(info.getId()==0) {
					mDAO.updateThread(info.getUrl(), info.getId(),(info.getEnd()-info.getStart()), info.getMd5(), "true", dateTools.getCurrentTime() );
				}else if(info.getId()!=0){
					mDAO.updateThread(info.getUrl(), info.getId(),(info.getEnd()-info.getStart()+1), info.getMd5(), "true",dateTools.getCurrentTime() );
				}
			}
			Intent intent = new Intent(IntentAction.ACTION_FINISH);
			mFileInfo.setOvertime(dateTools.getCurrentTime());
			intent.putExtra( KeyName.FILEINFO_TAG, mFileInfo);
			mContext.sendBroadcast(intent);
		}
	}






	/**
	 * 进行文件下载的线程
	 **/
	class DownloadThread extends Thread{
		/**
		 * 文件下载线程的信息
		 */
		private ThreadInfo mThreadInfo = null ;
		/**
		 * 标识线程是否执行完成
		 */
		public Boolean isFinished = false ;
		/**
		 * 广播下载进度的间隔时间
		 */
		private final static int BROADCAST_TIME = 100 ;
		/**
		 * httpUrl连接
		 */
		HttpURLConnection conn = null ;
		/**
		 * 任意写入文件：RandomAccessFile
		 */
		RandomAccessFile raf= null;
		/**
		 * 输入流
		 */
		InputStream input = null ;

		/**
		 * 进行文件下载的线程
		 * @param mThreadInfo 文件下载线程的信息
		 **/
		public DownloadThread(ThreadInfo mThreadInfo) {
			this.mThreadInfo = mThreadInfo;
		}

		@Override
		public void run(){
			try {
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");

				//设置url上资源下载位置/范围
				int start = mThreadInfo.getStart() +mThreadInfo.getFinished();
				conn.setRequestProperty("Range", "bytes=" +start +"-" + mThreadInfo.getEnd() );

				//设置文件本地写入位置
				File file = new File( DownloadConfig.DOWNLOAD_PATH, mFileInfo.getFileName() );
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				mFinishedLen +=mThreadInfo.getFinished();

				//开始下载啦
				if ( conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT ){
					//读取数据
					input = conn.getInputStream();
					byte[] buffer = new byte[ 1024 * 4 ];
					int len = -1;
					long time = System.currentTimeMillis();
					while((len = input.read(buffer))!=-1){
						//写入文件
						raf.write(buffer,0,len);

						mFinishedLen += len ;
						mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
						if (System.currentTimeMillis()-time > BROADCAST_TIME ) {  //每500ms刷新一次
							time = System.currentTimeMillis();
							double progress_result = (double)mFinishedLen / (double)mFileInfo.getLength();  // 计算下载进度
							double download_rate = (double)len / (double)(1024*BROADCAST_TIME/1000);  		// 计算下载速率

							Intent  intent  = new Intent( IntentAction.ACTION_UPDATE);
							intent.putExtra( KeyName.FINISHED_TAG,progress_result );
							intent.putExtra( KeyName.DOWNLOAD_RATE_TAG,download_rate );
							intent.putExtra("id", mFileInfo.getId() );
							mContext.sendBroadcast(intent);
						}
						//下载暂停时，保存下载进度搭配数据库
						if (isPause) {
							mDAO.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished(), mThreadInfo.getMd5(), "false", "none" );
							return;
						}
						// 下载连接获取输入流或文件写入失败，清空文件下载进度，并重新下载
						if (raf ==null || input ==null ){
							mDAO.updateThread(mThreadInfo.getUrl(), 0, 0, mThreadInfo.getMd5(), "false" , "none");
							mDAO.updateThread(mThreadInfo.getUrl(), 1, 0, mThreadInfo.getMd5(), "false" , "none");
							mDAO.updateThread(mThreadInfo.getUrl(), 2, 0, mThreadInfo.getMd5(), "false" , "none");

							Intent  intent  = new Intent( IntentAction.ACTION_UPDATE);
							intent.putExtra( KeyName.FINISHED_TAG, 0 );
							intent.putExtra( KeyName.DOWNLOAD_RATE_TAG, 0 );
							intent.putExtra("id", mFileInfo.getId() );
							mContext.sendBroadcast(intent);
							download();
						}
					}
					isFinished = true ;// 标识线程执行完毕

					// 检查下载任务是否执行完毕
					checkAllThreadsFinished(mThreadInfo );
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{//关闭连接
				try {
					if (raf !=null) {
						raf.close();
					}else {
						System.out.println("DOwnloadTask-280行 RadomAccessFile发生错误");
					}if (input !=null) {
						input.close();
					}else {
						System.out.println("DOwnloadTask-280行 inputStream发生错误");
					}if (conn !=null) {
						conn.disconnect();
					}else {
						System.out.println("DOwnloadTask-280行 HttpURLConnection发生错误");
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//--------------------
	//--------------------
}
