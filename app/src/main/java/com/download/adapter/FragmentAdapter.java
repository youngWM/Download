package com.download.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class FragmentAdapter extends FragmentPagerAdapter {

    /**
     * 这个是存放Fragment的数组列表
     */
    private ArrayList<Fragment> fragmentArray;


    /**
     * fragment适配器
     * @param fm  FragmentManager
     * @param mFragmentArray  存放Fragment的数组列表
     */
    public FragmentAdapter(FragmentManager fm, ArrayList<Fragment> mFragmentArray) {
        this(fm);
        this.fragmentArray = mFragmentArray;
    }

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    //这个函数的作用是当切换到第arg0个页面的时候调用。
    @Override
    public Fragment getItem(int arg0) {
        return this.fragmentArray.get(arg0);
    }

    @Override
    public int getCount() {
        return this.fragmentArray.size();
    }


}
