package com.crakac.ofuton.user;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.TweetActivity;
import com.crakac.ofuton.util.AppUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class UserDetailActivity extends FragmentActivity {
	private ViewPager pager;
	private PagerSlidingTabStrip tab;
	private SimpleFragmentPagerAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		String title = getIntent().getExtras().getString("screenName");

		UserDetailFragment fragment = new UserDetailFragment();
		Bundle b = new Bundle();
		b.putString("screenName", title);
		fragment.setArguments(b);

		adapter = new SimpleFragmentPagerAdapter(
				getSupportFragmentManager());
		adapter.setFragment(fragment);
		adapter.setTitle(title);
		
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		
		tab = (PagerSlidingTabStrip) findViewById(R.id.pagerTabStrip);
		tab.setIndicatorColorResource(android.R.color.white);
		tab.setViewPager(pager);
		
		final Context context = this;
		ImageView iv = (ImageView) findViewById(R.id.tweetEveryWhere);
		iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(context, TweetActivity.class));
			}
		});
	}
}
