package com.crakac.ofuton;

import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.util.AppUtil;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class ShowConversationActivity extends FragmentActivity {
	private ViewPager viewPager;
	private PagerTabStrip pagerTabStrip;
	private FragmentPagerAdapter pagerAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		pagerAdapter = new TimelineFragmentPagerAdapter(
				getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(1);
		pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
		pagerTabStrip.setTabIndicatorColorResource(android.R.color.white);
		pagerTabStrip.setDrawFullUnderline(true);
		final Context context = this;
		pagerTabStrip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppUtil.showToast(context, "tapped");
			}
		});
	}
}
