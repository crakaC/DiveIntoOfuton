package com.crakac.ofuton;

import java.util.List;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.accounts.SelectAccountActivity;
import com.crakac.ofuton.lists.ListObserver;
import com.crakac.ofuton.lists.ListSelectActivity;
import com.crakac.ofuton.status.StatusHolder;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class MainActivity extends FragmentActivity {

	private PagerSlidingTabStrip tabs;
	private ViewPager viewPager;
	private TimelineFragmentPagerAdapter pagerAdapter;
	private ImageView composeBtn;
	private Context selfContext;
	private static final String TAG = MainActivity.class.getSimpleName();
	
	public MainActivity() {
		selfContext = this;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!TwitterUtils.existCurrentUser(this)) {
			Intent intent = new Intent(this, SelectAccountActivity.class);
			startActivity(intent);
			finish();
		} else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_main);
			tabs = (PagerSlidingTabStrip)findViewById(R.id.pagerTabStrip);
			viewPager = (ViewPager) findViewById(R.id.pager);
			pagerAdapter = new TimelineFragmentPagerAdapter(
					getSupportFragmentManager());

			viewPager.setAdapter(pagerAdapter);
			viewPager.setCurrentItem(2);

			tabs.setIndicatorColorResource(android.R.color.white);
			tabs.setDividerColorResource(android.R.color.white);
			
			composeBtn = (ImageView)findViewById(R.id.tweetEveryWhere);
			composeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, TweetActivity.class);
					startActivity(intent);
				}
			});
			StatusHolder.setContext(this);
			
			setPages(pagerAdapter);
			viewPager.setCurrentItem(2);
			tabs.setViewPager(viewPager);
		}
	}
	
	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart()");
		if(ListObserver.isChanged()){
			ListObserver.init();
			finish();
			startActivity(new Intent(getIntent()));
		}
		super.onResume();
	}
	/**
	 * set pages to pagerAdapter
	 */
	private void setPages(TimelineFragmentPagerAdapter adapter){
		adapter.addFavoriteTimeline();
		adapter.addMentionsTimeline();
		adapter.addHomeTimeline();
		List<TwitterList> lists = TwitterUtils.getCurrentUserLists(selfContext);
		for(TwitterList list : lists){
			adapter.addList(list.getListId(), list.getName());
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		finish();
		startActivity(getIntent());
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_lists:
			startActivity(new Intent(this, ListSelectActivity.class));
			break;
		case R.id.menu_accounts:
			Intent acountSetting = new Intent(this, SelectAccountActivity.class);
			startActivity(acountSetting);
		}
		return super.onOptionsItemSelected(item);
	}
}
