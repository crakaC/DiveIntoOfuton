package com.crakac.fallintoofuton;

import com.crakac.fallintoofuton.util.AppUtil;
import com.crakac.fallintoofuton.util.TwitterUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterOauthActivity extends Activity {

	private static final String REQUEST_TOKEN = "request_token";
	private String mCallbackURL;
	private Twitter mTwitter;
	private RequestToken mRequestToken;
	private static final String TAG = TwitterOauthActivity.class.getSimpleName();

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
		setContentView(R.layout.activity_twitter_oauth);
		
		mCallbackURL = getString(R.string.twitter_callback_url);
		mTwitter = TwitterUtils.getTwitterInstanceForAuth(this);
		
		findViewById(R.id.action_start_oauth).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startAuthorize();
			}
		});
	}
	
	/**
	 * start oauth
	 */
	private void startAuthorize(){
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

	@Override
	public void onNewIntent(Intent intent) {
		
		if(intent == null
				|| intent.getData() == null
				|| !intent.getData().toString().startsWith(mCallbackURL)){
			return;
		}
		String verifier = intent.getData().getQueryParameter("oauth_verifier");
		
		AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>(){

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
				if (accessToken != null){
					AppUtil.showToast(getApplicationContext(),"Success authorization");
					successOAuth(accessToken);
				} else {
					AppUtil.showToast(getApplicationContext(),"Authorization failed");
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
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
