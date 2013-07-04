package com.crakac.ofuton.acounts;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.acounts.AcountListFragment.ClickFooterListner;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class AcountSelectActivity extends FragmentActivity implements ClickFooterListner{
	private Twitter mTwitter;
	private RequestToken mRequestToken;
	private String mCallbackURL;
	private static final String REQUEST_TOKEN = "request_token";
	private static final String TAG = AcountSelectActivity.class.getSimpleName();
	private Context mContext;

	private ViewPager pager;
	private PagerSlidingTabStrip tab;
	private SimpleFragmentPagerAdapter adapter;
	private ProgressDialogFragment progressDialog;
	
	private AcountListFragment mFragment;
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
		mContext = this;
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
		
		mTwitter = TwitterUtils.getTwitterInstanceForAuth(this);
		mCallbackURL = getString(R.string.twitter_callback_url);
		mFragment = new AcountListFragment();
		adapter.setFragment(mFragment);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");
		if(intent == null
				|| intent.getData() == null
				|| !intent.getData().toString().startsWith(mCallbackURL)){
			return;
		}
		String verifier = intent.getData().getQueryParameter("oauth_verifier");
		AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>(){

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialogFragment.newInstance("認証中です");
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.add(progressDialog, "loading");
				transaction.commitAllowingStateLoss();
				//progressDialog.show(getSupportFragmentManager(), "progress_dialog");
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
				progressDialog.dismiss();
				if (accessToken != null){
					successOAuth(accessToken);
				} else {
					AppUtil.showToast(getApplicationContext(),"認証に失敗しました");
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
		AsyncTask<AccessToken, Void, Boolean> task = new AsyncTask<AccessToken, Void, Boolean>() {
			
			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialogFragment.newInstance("ユーザー情報を取得しています");
				progressDialog.show(getSupportFragmentManager(), "progress");
			}

			@Override
			protected Boolean doInBackground(AccessToken... params) {
				TwitterUtils.storeAccessToken(mContext, params[0]);
				return TwitterUtils.addUser(mContext);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				progressDialog.dismiss();
				if(result){
					finish();
					startActivity(getIntent());
				} else {
					AppUtil.showToast(mContext, "すでに登録されています");
				}
			}
		};
		task.execute(accessToken);
		}

	@Override
	public void onClickFooter() {
	/**
	 * start oauth
	 */
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>(){
			
			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialogFragment.newInstance("しばらくお待ちください");
				progressDialog.show(getSupportFragmentManager(), "progress");
			}

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
				progressDialog.dismiss();
				if (url != null) {
					Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(intent);
					//finish();//finishするとRequestoTokenが死ぬので無理ですね．
				} else {
					// mistake
				}
			}
		};
		task.execute();
	}
}