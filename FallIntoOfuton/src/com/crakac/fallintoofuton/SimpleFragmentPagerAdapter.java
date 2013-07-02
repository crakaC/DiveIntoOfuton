package com.crakac.fallintoofuton;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter{
	private Fragment mFragment;
	private String title;
	
	public SimpleFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	public Fragment getItem(int arg0) {
		return mFragment;
	}
	@Override
	public int getCount() {
		return 1;
	}
	public CharSequence getPageTitle(int position) {
		return title;
	}
	public void setFragment(Fragment fragment){
		mFragment = fragment;
	}
	public void setTitle(String title){
		this.title = title;
	}
}