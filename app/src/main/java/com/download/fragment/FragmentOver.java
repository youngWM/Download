package com.download.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.download.adapter.OverAdapter;
import com.download.entities.FileInfo;
import com.download.entities.IntentAction;
import com.download.entities.KeyName;
import com.download.pulltoresh.RefreshableView;
import com.download.pulltoresh.RefreshableView.PullToRefreshListener;
import com.example.download.R;

import java.util.ArrayList;

/**
 * 存放完成下载任务文件
 */
public class FragmentOver extends Fragment {

	/**
	 * layout 
	 */
	private View mView = null;
	/**
	 * 【已下载】listView列表 
	 */
	private ListView mListView = null;
	/**
	 * 下拉刷新 
	 */
	private RefreshableView mRefreshableView = null ;


	/**
	 * 存储用于【已下载】fragment中的listView
	 */
	ArrayList<FileInfo> mFileList= null;
	/**
	 * 完成下载listView适配器
	 */
	OverAdapter mAdapter = null ;
	/**
	 * 接收完成下载的文件信息并给添加进over中的listview 
	 */
	Handler handler  = null ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// 实例化控件对象
		this.mView = inflater.inflate(R.layout.fragment_over, null);
		mListView =(ListView) mView.findViewById( R.id.lv_over);
		mRefreshableView = (RefreshableView) mView.findViewById(R.id.refreshable_view_over);
		mFileList = new ArrayList<FileInfo>() ;
		mAdapter = new OverAdapter(getActivity(), mFileList);
		mListView.setAdapter(mAdapter);
		mAdapter.initListView();

		// 注册广播接收器，接收下载进度信息和结束信息
		IntentFilter filter = new IntentFilter();
		filter.addAction( IntentAction.ACTION_FINISH );
		getActivity().registerReceiver( mReceiver, filter );
		// 下拉刷新：显示已下载的文件
		mRefreshableView.setOnRefreshListener(new myPullToRefreshListener() ,1);
		return this.mView ;
	}

	@Override
	public void onDestroyView() {
		getActivity().unregisterReceiver(mReceiver);
		super.onDestroyView();
	}


	/**
	 * 更新UI的广播接收器
	 **/
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 下载结束,添加已下载文件listview
			if( IntentAction.ACTION_FINISH.equals(intent.getAction()) ){
				FileInfo fileInfo = (FileInfo) intent.getSerializableExtra( KeyName.FILEINFO_TAG );
				mAdapter.addListViewItem(fileInfo);
			}
		}
	};


	/**
	 * 下拉刷新监听事件：接收完成下载文件的广播，显示已下载的文件
	 */
	public class myPullToRefreshListener implements PullToRefreshListener{
		/**
		 * 下拉刷新监听事件
		 */
		public myPullToRefreshListener() {
		}

		/**
		 * 下拉刷新
		 **/
		@Override
		public void onRefresh() {
			mRefreshableView.finishRefreshing();
		}
	}

}
