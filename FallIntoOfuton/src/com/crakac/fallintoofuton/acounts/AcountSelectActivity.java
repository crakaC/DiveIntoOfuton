package com.crakac.fallintoofuton.acounts;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.fallintoofuton.SimpleFragmentPagerAdapter;
import com.crakac.fallintoofuton.acounts.AcountListFragment.ClickFooterListner;
import com.crakac.fallintoofuton.util.AppUtil;
import com.crakac.fallintoofuton.util.TwitterUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class AcountSelectActivity extends FragmentActivity implements ClickFooterListner{
	private Twitter mTwitter;
	private RequestToken mRequestToken;
	private String mCallbackURL;
	private static final String REQUEST_TOKEN = "request_token";

	private ViewPager pager;
	private PagerSlidingTabStrip tab;
	private SimpleFragmentPagerAdapter adapter;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(REQUEST_TOKEN, mRequestToken);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mRequestToken = (RequestToken) savedInstanceState.getSerializable(REQUEST_TOKEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		ImageView iv = (ImageView)findViewById(R.id.tweetEveryWhere);
		iv.setVisibility(View.GONE);
		
		adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
		adapter.setTitle("Acount");
		
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		
		tab = (PagerSlidingTabStrip) findViewById(R.id.pagerTabStrip);
		tab.setIndicatorColorResource(android.R.color.white);
		tab.setViewPager(pager);
		
		mTwitter = TwitterUtils.getTwitterInstance(this);
		mCallbackURL = getString(R.string.twitter_callback_url);
		AcountListFragment fragment = new AcountListFragment();
		adapter.setFragment(fragment);
	}

	@Override
	public void onNewIntent(Intent intent) {
		
		if(intent == null
				|| intent.getData() == null
				|| !intent.getData().toString().startsWith(mCallbackURL)){
			return;
		}
		String verifier = intent.getData().getQueryParameter("oauth_verifier");
		AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>(){
			ProgressDialogFragment dialog;
			
			@Override
			protected void onPreExecute() {
				dialog = ProgressDialogFragment.newInstance("‚µ‚Î‚ç‚­‚¨‘Ò‚¿‚­‚¾‚³‚¢");
				dialog.show(getSupportFragmentManager(), "progress_dialog");
			}

			@Override
			protected AccessToken doInBackground(String... params) {
				try {
					return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(AccessToken accessToken) {
				dialog.dismiss();
				if (accessToken != null){
					AppUtil.showToast(getApplicationContext(),"Success authorization");
					successOAuth(accessToken);
				} else {
					AppUtil.showToast(getApplicationContext(),"Fail Authorization");
				}
			}
		};
		task.execute(verifier);
	}
	/**
	 * Store AccessToken and user infomation 
	 * @param accessToken
	 * 
	 */
	private void successOAuth(AccessToken accessToken){
		TwitterUtils.storeAccessToken(this, accessToken);
		TwitterUtils.storeUserInfo(this);
	}

	@Override
	public void onClickFooter() {
	/**
	 * start oauth
	 */
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>(){
			@Override
			protected String doInBackground(Void... params) {
				try {
					mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
					return mRequestToken.getAuthorizationURL();
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String url) {
				if (url != null) {
					Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(intent);
				} else {
					// mistake
				}
			}
		};
		task.execute();
	}
}