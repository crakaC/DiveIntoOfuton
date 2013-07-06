package com.crakac.ofuton.user;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.TimelineFragmentPagerAdapter;
import com.crakac.ofuton.TweetActivity;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ProgressDialogFragment;
import com.crakac.ofuton.util.TwitterUtils;
import com.loopj.android.image.SmartImageView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserDetailActivity extends FragmentActivity {
	protected static final String TAG = UserDetailActivity.class.getSimpleName();
	private ViewPager pager;
	private PagerSlidingTabStrip tab;
	private TimelineFragmentPagerAdapter adapter;
	private Context mContext;
	
	private TextView title, bio, location, url, relation;
	private SmartImageView icon;
	private AsyncTask<String, Void, twitter4j.User> loadContentTask;
	private AsyncTask<Long, Void, twitter4j.Relationship> loadRelationTask;
	private ProgressDialogFragment dialog;
	private FragmentManager manager;
	private Twitter twitter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.userdetail_activityl);
		
		mContext = this;
		manager = getSupportFragmentManager();
		twitter = TwitterUtils.getTwitterInstance(mContext);
		
		adapter = new TimelineFragmentPagerAdapter(
				getSupportFragmentManager());
		adapter.addHomeTimeline();
		adapter.addMentionsTimeline();
		
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		
		tab = (PagerSlidingTabStrip) findViewById(R.id.tab);
		tab.setIndicatorColorResource(android.R.color.white);
		tab.setViewPager(pager);
		
		findAndInitViews();
		loadContent();
		
		final Context context = this;
		ImageView iv = (ImageView) findViewById(R.id.tweetEveryWhere);
		iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(context, TweetActivity.class));
			}
		});
		
	}
	
	private void findAndInitViews(){
		//xmlの方にはダミーのテキストを入れているので．
		title = (TextView) findViewById(R.id.title);
		bio = (TextView) findViewById(R.id.bioText);
		location = (TextView) findViewById(R.id.locationText);
		url = (TextView) findViewById(R.id.urlText);
		icon = (SmartImageView)findViewById(R.id.icon);
		relation = (TextView) findViewById(R.id.relationText);
		
		title.setText("読込中");
		bio.setText("");
		location.setText("");
		url.setText("");
		relation.setText("");
	}
	
	private void loadContent(){
		if(loadContentTask != null && loadContentTask.getStatus() == AsyncTask.Status.RUNNING){
			return;
		}
		loadContentTask = new AsyncTask<String, Void, twitter4j.User>() {
			@Override
			protected void onPreExecute() {
				dialog = ProgressDialogFragment.newInstance("ユーザー情報を読み込んでいます");
				dialog.show( manager , "user detail");
			}

			@Override
			protected twitter4j.User doInBackground(String... params) {
				try {
					return twitter.showUser(params[0]);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(twitter4j.User result) {
				dialog.dismiss();
				if(result != null){
					setContent(result);
					getRelationShip(result);
				} else {
					AppUtil.showToast(mContext, "ユーザー情報を取得出来ませんでした");
					finish();
				}
			}
		};
		loadContentTask.execute(getIntent().getStringExtra("screenName"));
	}
	
	private void setContent(twitter4j.User user){
		title.setText(user.getName() + "(@" + user.getScreenName() + ")");
		bio.setText(user.getDescription());
		location.setText(user.getLocation());
		url.setText(user.getURL());
		icon.setImageUrl(user.getBiggerProfileImageURL());
	}
	
	private void getRelationShip(twitter4j.User user){
		if(loadRelationTask != null && loadRelationTask.getStatus() == AsyncTask.Status.RUNNING){
			return;
		}
		loadRelationTask = new AsyncTask<Long, Void, twitter4j.Relationship>() {
			@Override
			protected twitter4j.Relationship doInBackground(Long... params) {
				try {
					return twitter.showFriendship(params[0], params[1]);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(twitter4j.Relationship result) {
				if(result != null){
					if(result.isSourceBlockingTarget()){
						relation.setText("ブロック中");
					} else if(result.isSourceFollowedByTarget() && result.isSourceFollowingTarget()){
						relation.setText("相互フォロー");
					} else if(result.isSourceFollowingTarget()){
						relation.setText("片思い");
					} else if(result.isSourceFollowedByTarget()){
						relation.setText("ファン");
					} else {
						relation.setText("無関心");
					}
				} else {
					AppUtil.showToast(mContext, "何かがおかしいよ");
					Log.d(TAG, "getRelaitionship(), something wrong");
				}
			}
		};
		long currentUserId = TwitterUtils.getCurrentUserId(mContext);
		if(user.getId() == currentUserId){
			relation.setText("それはあなたです！");
			return;
		}
		loadRelationTask.execute(currentUserId, user.getId());
	}
}
