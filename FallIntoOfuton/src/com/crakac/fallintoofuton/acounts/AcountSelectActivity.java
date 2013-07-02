package com.crakac.fallintoofuton.acounts;


import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.fallintoofuton.SimpleFragmentPagerAdapter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class AcountSelectActivity extends FragmentActivity {
	private ViewPager pager;
	private PagerSlidingTabStrip tab;
	private SimpleFragmentPagerAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		ImageView iv = (ImageView)findViewById(R.id.tweetEveryWhere);
		iv.setVisibility(View.GONE);
		
		adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
		adapter.setFragment(new AcountListFragment());
		adapter.setTitle("Acount");
		
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		
		tab = (PagerSlidingTabStrip) findViewById(R.id.pagerTabStrip);
		tab.setIndicatorColorResource(android.R.color.white);
		tab.setViewPager(pager);
	}
}