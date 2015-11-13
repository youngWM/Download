package com.download.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.download.adapter.DownloadAdapter;
import com.download.entities.DownloadConfig;
import com.download.entities.FileInfo;
import com.download.entities.IntentAction;
import com.download.entities.KeyName;
import com.download.pulltoresh.RefreshableView;
import com.download.pulltoresh.RefreshableView.PullToRefreshListener;
import com.download.tools.MD5Util;
import com.download.tools.Tools;
import com.download.tools.URLTools;
import com.example.download.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class FragmentDownload extends Fragment {
	/**
	 * layout
	 */
	private View mView ;
	/**
	 * listview列表
	 */
	private ListView mListView ;
	/**
	 * 一键全部下载
	 */
	private Button mBtnDownAll;
	/**
	 * 下拉视图
	 */
	private RefreshableView mRefreshView ;
	/**
	 * listview适配器
	 */
	private DownloadAdapter mAdapter = null;


	/**
	 * 本地数据库中的url与web上接收的url集
	 */
	private List<String> urlList = new ArrayList<String>();
	/**
	 * 接收web-service返回的json数据，并处理
	 */
	private Handler handler ;
	/**
	 * 存放接收到的web端的信息，用以打印查看
	 */
	private String result = "";



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		//-------------------
		//实例化控件对象
		this.mView = inflater.inflate(R.layout.fragment_download, null);
		mRefreshView = (RefreshableView) mView.findViewById(R.id.refreshable_view_download);
		mListView =(ListView) mView.findViewById(R.id.lv_download);
		mBtnDownAll = (Button) mView.findViewById(R.id.download_all);
		mAdapter = new DownloadAdapter(getActivity(),mListView);

		mListView.setAdapter(mAdapter);
		urlList= mAdapter.initListView();

		// 注册广播接收器，接收下载进度信息和结束信息
		IntentFilter filter = new IntentFilter();
		filter.addAction( IntentAction.ACTION_UPDATE);
		filter.addAction( IntentAction.ACTION_FINISH);
		getActivity().registerReceiver(mReceiver, filter);

		//下拉刷新：发送下载请求给web端,并处理应答数据
		mRefreshView.setOnRefreshListener(new myPullToRefreshListener(),0);

		mBtnDownAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if( mBtnDownAll.getText().equals("全部暂停") ){
					mBtnDownAll.setText("全部下载");
					mAdapter.setStopDownloadAll();
				}else if ( mBtnDownAll.getText().equals("全部下载") ){
					mBtnDownAll.setText("全部暂停");
					mAdapter.setStartDownloadAll();
				}
			}
		});

		return this.mView ;
	}

	/**
	 * 应用关闭时关闭广播接收器、暂停所有下载，并记录下载进度
	 */
	@Override
	public void onDestroyView() {
		mAdapter.setStopDownloadAll();
		getActivity().unregisterReceiver(mReceiver);
		super.onDestroyView();
	}

	/**
	 * 下拉刷新监听事件
	 */
	public class myPullToRefreshListener implements PullToRefreshListener{

		public myPullToRefreshListener() {
			// 创建一个Handler对象，处理web发回的json格式url上的文件
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (result != null) {
						//-------------------
						//创建下载文件对象信息
						try {
							URLTools urlTools = new URLTools();
							List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
							JSONObject jsonobj   = new JSONObject( String.valueOf(result) );
							Log.i("md5", "FragmentDownload-131行 jsonobj：  "+jsonobj);
							JSONObject jsonobj_url = jsonobj.getJSONObject("URL");
							JSONObject jsonobj_md5 = jsonobj.getJSONObject("MD5");
							JSONArray  array_key = jsonobj_url.names();	// 获取json中的key并存放入数组

							for (int i = 0; i < array_key.length(); i++) {
								String url      = jsonobj_url.getString(array_key.opt(i).toString());
								String md5_web  = jsonobj_md5.getString(array_key.opt(i).toString());
								Log.i("md5", "FragmentDownload-131行  "+url+"  的【验证】md5值为：    "+ md5_web);
								// 将url从绝对路径转换为虚拟路径
								String filename = urlTools.getURLFileName(url, "\\") ;
								String  urlStr  = DownloadConfig.getURL_DOWNLOAD_PATH() + filename;
								// 增加listview中没有的URL对应文件
								if ( !urlList.contains(urlStr) ){
									urlList.add(urlStr);
									FileInfo fileInfo  = new FileInfo( 0,0,urlStr,filename,0,0, md5_web, "false", "false", "none" );
									fileInfoList.add(fileInfo);
								}
							}
							mAdapter.addListViewItem(fileInfoList);
							//---------------
						} catch (JSONException e) {
							e.printStackTrace();
						}
						result = "";
					}
					super.handleMessage(msg);
				}
			};
		}

		/**
		 * 下拉刷新发送请求信息给web端
		 **/
		@Override
		public void onRefresh() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					send("请求下载");
					Message m = handler.obtainMessage(); // 获取一个Message
					handler.sendMessage(m); 			 // 处理接收到的数据
					mRefreshView.finishRefreshing();
				}
			}).start();
		}
	}



	/**
	 * 发送文本内容到Web服务器
	 */
	public void send(String content) {
		//------------------
		URL url = null ;
		Tools tools = new Tools();
		String url_target = DownloadConfig.getURL_TARGET()+tools.base64(content);	  //要访问的URL地址，包含发送信息
		try {
			url = new URL(url_target);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();	  // 创建一个HTTP连接
			InputStreamReader in  = new InputStreamReader( urlConn.getInputStream()); // 获得读取的内容
			BufferedReader buffer = new BufferedReader(in); // 获取输入流对象
			String inputLine = null;
			//---------------------
			// 通过循环逐行读取输入流中的内容
			while ((inputLine = buffer.readLine()) != null) {
				result += inputLine + "\n";
			}
			// 关闭字符输入流对象,断开连接
			in.close();
			urlConn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 广播接收器：更新UI和数据库
	 **/
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//------------------
			//进行下载：更新下载进度和速度
			if ( IntentAction.ACTION_UPDATE.equals(intent.getAction())) {
				double finished = intent.getDoubleExtra( KeyName.FINISHED_TAG, 0 );
				double rate = intent.getDoubleExtra( KeyName.DOWNLOAD_RATE_TAG, 0 );
				int id = intent.getIntExtra( KeyName.ID_TAG, 0);
				mAdapter.updateListView( id, finished*100, rate );

				//------------------
				//下载结束：验证md5值并提示，并更新数据库
			}else if ( IntentAction.ACTION_FINISH.equals( intent.getAction()) ) {
				MD5Util md5Util = new MD5Util();
				FileInfo fileInfo = (FileInfo) intent.getSerializableExtra( KeyName.FILEINFO_TAG );
				//----------------
				try {
					if( md5Util.isMD5Equal(fileInfo) ) {
						mAdapter.updateListView( fileInfo.getId(), 100, 0 );
						URLTools urlTools = new URLTools();
						urlTools.toChinese(fileInfo);
						Toast.makeText( getActivity(), URLDecoder.decode(fileInfo.getFileName(), "UTF-8")
								+"下载成功！", Toast.LENGTH_SHORT ).show();
					}else{
						mAdapter.updateListView(fileInfo.getId(), 0, 0);
						Toast.makeText( getActivity(), URLDecoder.decode(fileInfo.getFileName(), "UTF-8")
								+" 文件下载出错，请重新下载！", Toast.LENGTH_LONG).show();
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	};

	//-----------------------
	//-----------------------
}
