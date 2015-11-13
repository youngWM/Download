package com.download.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.download.db.ThreadDAOIImpl;
import com.download.entities.FileInfo;
import com.example.download.R;
import com.example.download.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * 文件列表适配器
 */
public class OverAdapter extends BaseAdapter {

	/**
	 * 上下文
	 */
	private Context mContext = null ;
	/**
	 * FileInfo集合列表
	 */
	private List<FileInfo> mFileList = null ;
	/**
	 * 数据库访问接口的实现
	 */
	ThreadDAOIImpl mDAO ;
	ViewHolder holder = null ;
	/**
	 * @param mContext 上下文
	 * @param mFileList FileInfo集合列表
	 */
	public OverAdapter(Context mContext, List<FileInfo> mFileList) {
		super();
		this.mContext = mContext;
		this.mFileList = mFileList;
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
	 * 第二个参数convertView---旧视图 ：
	 * 展示在界面上的一个item。因为手机屏幕就那么大，所以一次展示给用户看见的内容是固定的，
	 * 如果你List中有1000条数据，不应该new1000个converView，那样内存肯定不足，应该学会控件
	 * 重用，滑出屏幕的converView就在下面新进来的item中重新使用，只是修改下他展示的值
	 */
	@Override
	public View getView(final int position, View view, ViewGroup ViewGroup) {
		try {
			final FileInfo fileInfo = mFileList.get(position);
			if( view == null ){
				view = LayoutInflater.from(mContext).inflate(R.layout.item_over, null );
				holder = new ViewHolder();
				holder.tvFileName = (TextView) view.findViewById(R.id.over_tv_file_name);
				holder.tvOverTime = (TextView) view.findViewById(R.id.over_tv_time);
				holder.btnOverDel = (Button) view.findViewById(R.id.over_btn_delete);
				holder.checkBox   = (CheckBox) view.findViewById(R.id.over_checkBox);
				view.setTag(holder);  // view中setTag表示给View添加一个格外的数据，以后可以用getTag()将这个数据取出来，
			}else {					  // 把查找的view缓存起来方便多次重用
				holder = (ViewHolder) view.getTag();
			}
			holder.tvFileName.setText(URLDecoder.decode(fileInfo.getFileName(), "UTF-8"));   //解码url文件名并显示
			holder.tvOverTime.setText(fileInfo.getOvertime() );
			holder.btnOverDel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mDAO.deleteThread(fileInfo.getUrl() );
					mFileList.remove(fileInfo);
					notifyDataSetChanged();
				}
			});
			view.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					holder.btnOverDel.setVisibility(View.GONE);
					holder.checkBox.setVisibility(View.VISIBLE);
					return false;
				}
			});

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return view;
	}


	/**
	 * 往listview添加新项
	 * @param fileInfoList  新的FileInfo集
	 */
	public void addListViewItem(FileInfo fileInfo ){
		mFileList.add(0, fileInfo);
		notifyDataSetChanged();
	}


	public void longClick(){
		holder.btnOverDel.setVisibility(View.GONE);
		holder.checkBox.setVisibility(View.VISIBLE);
	}
	/**
	 * 获取数据库表中下载文件信息，并初始化ListView
	 * @return UrlList  返回数据库中所有文件的url集，供程序判断接收的url是否已存在
	 */
	public void initListView(){
		//-----------
		List<FileInfo> fileList = mDAO.getDBFileInfoList();
		for( FileInfo fileInfo : fileList ){
			if (fileInfo.getFinished() ==100 ) {
				mFileList.add(0,fileInfo);
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * ViewHolder 是一个临时的储存器，把每次getView方法中的每次返回的View缓存起来，
	 * 可以下次再用。这样的好处是不必每次都到布局文件中查找控件，只需调用ViewHoldr即可
	 **/
	private static class ViewHolder{// 定义为static内部静态类，这样就只生成一次，比较不好内存
		/**
		 * 显示已下载文件名
		 */
		TextView tvFileName;
		TextView tvOverTime;
		Button   btnOverDel;
		CheckBox checkBox ;
	}

}
