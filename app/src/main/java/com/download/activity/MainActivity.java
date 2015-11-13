package com.download.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.download.adapter.FragmentAdapter;
import com.download.fragment.FragmentDownload;
import com.download.fragment.FragmentOver;
import com.example.download.R;

import java.util.ArrayList;

/**
 * @author Administrator
 *
 */
public class MainActivity extends FragmentActivity {

	/**
	 * 点击【下载】文字所开启的点击事件相应项
	 */
	private final static int TV_DOWNLOAD = 0 ;
	/**
	 * 点击【已下载】文字所开启的点击事件相应项
	 */
	private final static int TV_OVER = 1 ;
	/**
	 * 【下载】状态显示
	 */
	private TextView mTvDownload;
	/**
	 * 【已下载】状态显示
	 */
	private TextView mTvOver;
	/**
	 * 存放fragment
	 */
	private ViewPager mViewPager;


	@Override
	protected void onCreate(Bundle arg0) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(arg0);
		setContentView(R.layout.main_activity);

		mTvDownload = (TextView) findViewById(R.id.tv_dowmload);
		mTvOver     = (TextView) findViewById(R.id.tv_over);
		initViewPager();
		mTvDownload.setOnClickListener(new MyOnClickListener(TV_DOWNLOAD));
		mTvOver.setOnClickListener(new MyOnClickListener(TV_OVER));
	}


	/**
	 * 初始化viewpager界面
	 */
	@SuppressWarnings("deprecation")
	private void initViewPager(){
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		//添加fragment页面
		ArrayList<Fragment> mFragmentArray = new ArrayList<Fragment>();
		mFragmentArray.add(new FragmentDownload());
		mFragmentArray.add(new FragmentOver() );
		FragmentAdapter mAdapter = new FragmentAdapter(getSupportFragmentManager(),mFragmentArray);
		mViewPager.setAdapter(mAdapter);

		//-设置初始化时默认显示的fragment
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());;
		mViewPager.setCurrentItem(0);
	}

	/**
	 * viewpager中fragment页面改变时触发的事件 
	 **/
	public class MyOnPageChangeListener implements OnPageChangeListener {
		//------------
		@SuppressWarnings("deprecation")
		public void onPageScrolled(int arg0, float arg1, int arg2) {//滑动ViewPager界面

			switch (mViewPager.getCurrentItem()) {
				//-------------
				//点击【下载】状态后的显示
				case TV_DOWNLOAD:
					mTvDownload.setBackgroundColor(getResources().getColor(R.color.tv_bgd_blue));
					mTvOver.setBackgroundColor(getResources().getColor(R.color.tv_bgd_gray));
					mTvDownload.setTextSize(22);
					mTvOver.setTextSize(18);
					break;

				//-------------
				//点击【已下载】状态后的显示
				case TV_OVER:
					mTvDownload.setBackgroundColor(getResources().getColor(R.color.tv_bgd_gray));
					mTvOver.setBackgroundColor(getResources().getColor(R.color.tv_bgd_blue));
					mTvDownload.setTextSize(18);
					mTvOver.setTextSize(22);
					break;

				default:
					break;
			}
		}

		public void onPageScrollStateChanged(int arg0) {
		}
		public void onPageSelected(int arg0) {
		}
	}


	public class MyOnClickListener implements OnClickListener{
		int index = 0;
		/**
		 * 状态栏点击事件；
		 * i = 0时，响应点击【下载】事件；
		 * i = 1时，响应点击【已下载】事件；
		 */
		public MyOnClickListener(int i) {
			index = i ;
		}

		@Override
		public void onClick(View v) {
			switch (index) {
				case TV_DOWNLOAD:
					mViewPager.setCurrentItem(TV_DOWNLOAD); // 点击【下载】切换fragment
					break;
				case TV_OVER:
					mViewPager.setCurrentItem(TV_OVER);     // 点击【下载】切换fragment
					break;

				default:
					break;
			}
		}

	}

}
