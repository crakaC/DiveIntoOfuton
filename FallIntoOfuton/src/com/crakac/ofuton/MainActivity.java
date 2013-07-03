package com.crakac.ofuton;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.acounts.AcountSelectActivity;
import com.crakac.ofuton.acounts.TwitterOauthActivity;
import com.crakac.ofuton.status.StatusHolder;
import com.crakac.ofuton.timeline.HomeTimelineFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
	private static final String PREF_NAME = "ListsInfo";
	private static final String LIST_NAMES = "ListNames";
	private static final String LIST_IDS = "ListsIds";
	private static final String LIST_NUMS = "ListsNums";
	
	public MainActivity() {
		selfContext = this;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!TwitterUtils.hasAccessToken(this)) {
			Intent intent = new Intent(this, AcountSelectActivity.class);
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
			//viewpagerindicator set item
			//tabs.setCurrentItem(2);
		}
	}

	/**
	 * set pages to pagerAdapter
	 */
	private void setPages(TimelineFragmentPagerAdapter adapter){
		adapter.addFavoriteTimeline();
		adapter.addMentionsTimeline();
		adapter.addHomeTimeline();
		List<TwitterList> lists = TwitterUtils.getLists(selfContext);
		for(TwitterList list : lists){
			adapter.addList(list.getListId(), list.getName());
		}
	}

	private void setLists(){
		Log.d(TAG, "called setListTabs()");
		AsyncTask<Void, Void, List<twitter4j.UserList>> task = new AsyncTask<Void, Void, List<twitter4j.UserList>>() {
			Twitter twitter;
			@Override
			protected void onPreExecute() {
				twitter = TwitterUtils.getTwitterInstance(selfContext);
			}
			@Override
			protected List<UserList> doInBackground(Void... params) {
				try {
					return twitter.getUserLists(twitter.getId());
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(List<UserList> result) {
				if(result != null){
//					SharedPreferences preference = selfContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//					SharedPreferences.Editor editor = preference.edit();					
					for(int i = 0; i < result.size(); i++){
						UserList list = result.get(i);
//						editor.putInt(LIST_IDS + i,list.getId());
//						editor.putString(LIST_NAMES + i, list.getName());
//						
						long userId = TwitterUtils.getUserId(selfContext);
						TwitterUtils.addList(selfContext, new TwitterList( userId, list.getId(), list.getName(), list.getFullName()));
					}
//					editor.putInt(LIST_NUMS, result.size());
//					editor.commit();
					AppUtil.showToast(selfContext, "ƒŠƒXƒg‚Ì’Ç‰Á‚ªI‚í‚è‚Ü‚µ‚½");
					finish();
					startActivity(getIntent());
				}
			}
		};
		task.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			setLists();
			break;
		case R.id.menu_acount:
			Intent acountSetting = new Intent(this, AcountSelectActivity.class);
			startActivity(acountSetting);
		}
		return super.onOptionsItemSelected(item);
	}
}
