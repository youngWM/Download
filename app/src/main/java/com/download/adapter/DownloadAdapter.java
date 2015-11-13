package com.download.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.download.db.ThreadDAOIImpl;
import com.download.entities.FileInfo;
import com.download.entities.IntentAction;
import com.download.entities.KeyName;
import com.download.servers.DownloadService;
import com.example.download.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 【下载】文件列表适配器
 */
public class DownloadAdapter extends BaseAdapter {
	/**
	 * listView全局变量
	 */
	ListView  mListView = null ;
	/**
	 * 上下文
	 */
	private Context mContext = null ;
	/**
	 * 下载文件信息集
	 */
	private List<FileInfo> mFileList = new ArrayList<FileInfo>() ;
	/**
	 * 设置下载速度和进度显示两位小数 
	 */
	DecimalFormat decimalFormat = null;
	/**
	 * 数据库访问接口的实现
	 */
	ThreadDAOIImpl mDAO ;
	/**
	 * 按键上显示文字“开始”
	 */
	private static final String DOWNLOAD = "开始";
	/**
	 * 按键上显示文字“暂停”
	 */
	private static final String PAUSE    = "暂停";
	/**
	 * 按键上显示文字“完成”
	 */
	private static final String OVER = "完成";
	/**
	 * 按键上显示文字“等待”
	 */
	private static final String WAIT = "等待";

	public int downloadProgressNum = 0 ;
	ViewHolder holder = null ;
	/**
	 * @param mContext 上下文
	 */
	public DownloadAdapter(Context mContext, ListView listView) {
		super();
		this.mContext = mContext;
		this.mListView= listView;
		decimalFormat = new DecimalFormat("#0.00");
		mDAO = new ThreadDAOIImpl(mContext);
	}

	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 第二个参数View---旧视图 ：展示在界面上的一个item。
	 * 因为手机屏幕就那么大，所以一次展示给用户看见的内容是固定的，
	 * 如果你List中有1000条数据，不应该new 1000个item，那样内存肯定不足，应该学会控件
	 * 重用，滑出屏幕的View就在下面新进来的item中重新使用，只是修改下它展示的值
	 */
	@Override
	public View getView(final int position, View view, ViewGroup ViewGroup) {
		//---------
		if(view == null){
			view = LayoutInflater.from(mContext).inflate(R.layout.item_download, null);
			holder = new ViewHolder();
			holder.tvFile = (TextView) view.findViewById(R.id.tvfilename);
			holder.btnstate = (Button)   view.findViewById(R.id.btnState);
			holder.tvProgress = (TextView) view.findViewById(R.id.tvProgress);
			holder.tvDownloadRate = (TextView) view.findViewById(R.id.tvDownloadRate);
			holder.progressBarFile = (ProgressBar) view.findViewById(R.id.pbProgress);
			//设置进度条总长为100
			holder.progressBarFile.setMax(100);
			view.setTag(holder);  // view中setTag表示给View添加一个格外的数据，以后可以用getTag()
		}					 	  // 将这个数据取出来，把查找的view缓存起来方便多次重用
		else {
			holder = (ViewHolder) view.getTag();
		}
		setData(holder, position);
		return view;
	}

	/**
	 * 令ListView可见区域显示进度、下载速度变更
	 * @param holder ListView中控件存储器
	 * @param position  ListView可见区域item的位置
	 */
	private void setData(final ViewHolder holder, int position ){
		//---------------------
		try {
			final FileInfo fileInfo = mFileList.get(position);
			fileInfo.setId(position);  // 更新item的位置，否则解决item增删后position随位置变化~~~~very important~~~~~~
			holder.tvFile.setText(URLDecoder.decode(fileInfo.getFileName(), "UTF-8") );// 解码url文件名,并显示

			// 若完成下载，显示提示完成；否则显示下载进度
			if( fileInfo.getFinished() == 100 ){
				holder.tvDownloadRate.setVisibility( View.INVISIBLE);
				holder.progressBarFile.setProgress( 100);
				holder.tvProgress.setText("已下载");
				holder.btnstate.setText(OVER);
				return ;
			}else{
				holder.tvDownloadRate.setVisibility( View.VISIBLE);
				holder.tvDownloadRate.setText( decimalFormat.format(fileInfo.getRate() ) +"KB/s" );
				holder.tvProgress.setText( decimalFormat.format(fileInfo.getFinished()) +"%" );
				holder.progressBarFile.setProgress( (int)fileInfo.getFinished() );
			}

			// 点击按键：开始/暂停文件下载
			holder.btnstate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//--------
					Intent intent = new Intent( mContext, DownloadService.class );
					if( holder.btnstate.getText().equals(DOWNLOAD)){
						fileInfo.setIsDownload("true");
						intent.setAction(IntentAction.ACTION_START);
						holder.btnstate.setText(PAUSE);
						downloadProgressNum ++ ;
					} else if( holder.btnstate.getText().equals(PAUSE)) {
						fileInfo.setIsDownload("false");
						intent.setAction(IntentAction.ACTION_PAUSE);
						holder.btnstate.setText(DOWNLOAD);
						downloadProgressNum -- ;
					}

					intent.putExtra( KeyName.FILEINFO_TAG, fileInfo );
					mContext.startService(intent); // 通过intent传递信息fileInfo给servers
				}
			});
			// 一键【全部下载/暂停】点击后，listView item中按键文字变更
			if (fileInfo.getIsDownload().equals("true")  ){
				holder.btnstate.setText(PAUSE);
			}else if (fileInfo.getIsDownload().equals("false")  ){
				holder.btnstate.setText(DOWNLOAD);
			}

		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 获取数据库表中下载文件信息，并初始化ListView
	 * @return UrlList  返回数据库中所有文件的url集，供程序判断接收的url是否已存在
	 */
	public List<String> initListView(){
		//-----------
		List<String> mUrlList = new ArrayList<String>();
		List<FileInfo> fileList = mDAO.getDBFileInfoList();
		mFileList = fileList ;
		notifyDataSetChanged();
		//-----------
		for( FileInfo fileInfo : fileList ){
			mUrlList.add( fileInfo.getUrl());
		}
		return mUrlList;
	}

	/**
	 * 一键下载所有未完成下载的文件
	 */
	public void setStartDownloadAll(){
		for( FileInfo fileInfo : mFileList ){
			fileInfo.setIsDownload("true");
			notifyDataSetChanged();

			Intent intent = new Intent(mContext, DownloadService.class);
			intent.putExtra( KeyName.FILEINFO_TAG, fileInfo );
			intent.setAction(IntentAction.ACTION_START);
			mContext.startService(intent);

		}
	}

	/**
	 * 一键暂停所有文件的下载
	 */
	public void setStopDownloadAll(){
		for (FileInfo fileInfo : mFileList ){
			fileInfo.setIsDownload("false");
			Intent intent = new Intent(mContext, DownloadService.class);
			intent.putExtra( KeyName.FILEINFO_TAG, fileInfo );
			intent.setAction(IntentAction.ACTION_PAUSE);
			mContext.startService(intent);
		}
		notifyDataSetChanged();
	}

	/**
	 * 往listview添加新项
	 * @param fileInfoList  新的FileInfo集
	 */
	public void addListViewItem(List<FileInfo> fileInfoList ){
		for (FileInfo fileInfo : fileInfoList ){
			mFileList.add(0, fileInfo);
		}
		notifyDataSetChanged();
	}

	/**
	 * 更新listView可视区域中item的进度条和下载进度、速度
	 * @param id         需要更新的文件在list中的id
	 * @param progres    需要更新的下载文件的下载进度
	 * @param rate		  需要更新的下载文件的下载速度
	 */
	public void updateListView(int id, double progres, double rate ){

		// 判断下载中的文件是否在可视区，是则更新进度和下载速度
		FileInfo fileInfo = mFileList.get(id);
		int start = mListView.getFirstVisiblePosition();  // 可见视图的首个item的位置
		int end   = mListView.getLastVisiblePosition();   // 可见视图的最后item的位置
		int  position = mFileList.indexOf(fileInfo);      // 获取需要更新进度的下载文件fileInfo的位置
		//----------
		if( position -start >= 0 && end -position >= 0 ){
			View view = mListView.getChildAt(position -start);
			if(view == null) {
				return;
			}
			//------------
			fileInfo.setRate( rate);
			fileInfo.setFinished( progres);
			ViewHolder holder = (ViewHolder) view.getTag();
			setData( holder, position);
		}
	}



	/**
	 * ViewHolder 是一个临时的储存器，把每次getView方法中的每次返回的View缓存起来，
	 * 可以下次再用。这样的好处是不必每次都到布局文件中查找控件，只需调用ViewHoldr即可
	 **/
	private static class ViewHolder{// 定义为static内部静态类，这样就只生成一次，比较不好内存
		/**
		 * 显示下载文件名
		 */
		TextView tvFile;
		/**
		 * 显示下载进度
		 */
		TextView tvProgress;
		/**
		 * 显示下载速率
		 */
		TextView tvDownloadRate ;
		/**
		 * 下载按键
		 */
		Button btnstate ;
		/**
		 * 下载进度条
		 */
		ProgressBar progressBarFile ;
	}

}
