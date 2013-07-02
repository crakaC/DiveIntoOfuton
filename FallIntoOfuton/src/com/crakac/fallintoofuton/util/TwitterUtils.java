/**
 * Created by Kosuke on 13/05/18.
 */

package com.crakac.fallintoofuton.util;
import com.crakac.fallintoofuton.R;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class TwitterUtils {
	private static final String TAG = TwitterUtils.class.getSimpleName();
	private static final String TOKEN = "token";
	private static final String TOKEN_SECRET = "tokenSecret";
	private static final String PREF_NAME = "accessToken";
	private static final String USER_INFO = "userInfo";
	private static final String USER_ID = "userId";
	private static final String USER_SCREEN_NAME = "userScreenName";

	/**
	 *  Get AsyncTwitter instance.
	 *
	 *  @param context
	 *  @return
	 */
	public static AsyncTwitter getAsyncTwitterInstance(Context context)
	{
		String consumerKey = context.getString(	R.string.twitter_consumer_key );
		String consumerSecret = context.getString( R.string.twitter_consumer_secret );

		AsyncTwitterFactory factory = new AsyncTwitterFactory();
		AsyncTwitter twitter = factory.getInstance();
		twitter.setOAuthConsumer( consumerKey, consumerSecret );

		if( hasAccessToken(context) ){
			twitter.setOAuthAccessToken( loadAccessToken( context ) );
		}
		return twitter;
	}
	
	/**
	 * Twitter instance
	 * @param context
	 * @return 
	 */
	public static Twitter getTwitterInstance(Context context)
	{
		String consumerKey = context.getString(	R.string.twitter_consumer_key );
		String consumerSecret = context.getString( R.string.twitter_consumer_secret );

		TwitterFactory factory = new TwitterFactory();
		Twitter twitter = factory.getInstance();
		twitter.setOAuthConsumer( consumerKey, consumerSecret );

		if( hasAccessToken(context) ){
			twitter.setOAuthAccessToken( loadAccessToken( context ) );
		}
		return twitter;
	}


	/**
	 * Store access_token to preference.
	 *
	 * @param context
	 * @param accessToken
	 */
	public static void storeAccessToken( Context context, AccessToken accessToken )
	{
		SharedPreferences preferences = context.getSharedPreferences( PREF_NAME, Context.MODE_PRIVATE );
		Editor editor = preferences.edit();
		editor.putString( TOKEN, accessToken.getToken() );
		editor.putString( TOKEN_SECRET, accessToken.getTokenSecret() );
		editor.commit();
	}

	/**
	 * load access_token from preference
	 * @param context
	 * @return
	 */
	public static AccessToken loadAccessToken( Context context )
	{
		SharedPreferences preferences = context.getSharedPreferences( PREF_NAME, Context.MODE_PRIVATE );
		String token = preferences.getString( TOKEN, null );
		String tokenSecret = preferences.getString( TOKEN_SECRET, null );
		if( token != null && tokenSecret != null ){
			return new AccessToken( token, tokenSecret );
		}else{
			return null;
		}
	}

	/**
	 * Return true if it has access token.
	 * @return
	 */
	public static boolean hasAccessToken( Context context )
	{
		return loadAccessToken( context ) != null;
	}

	/**
	 * Return Twitter instance without request token
	 * @param context
	 * @return
	 */
	public static Twitter getTwitterInstanceForAuth(
			Context context) {
		String consumerKey = context.getString(	R.string.twitter_consumer_key );
		String consumerSecret = context.getString( R.string.twitter_consumer_secret );

		TwitterFactory factory = new TwitterFactory();
		Twitter twitter = factory.getInstance();
		twitter.setOAuthConsumer( consumerKey, consumerSecret );
		
		return twitter;
	}

	/**
	 * store userId and userScreenName.
	 * @param context
	 * using context to call Context#getSharedPreference()
	 */
	public static void storeUserInfo(Context context) {
		Twitter twitter = getTwitterInstance(context);
		SharedPreferences preferences = context.getSharedPreferences( USER_INFO, Context.MODE_PRIVATE );
		Editor editor = preferences.edit();
		try {
			editor.putLong( USER_ID, twitter.getId() );
			editor.putString( USER_SCREEN_NAME, twitter.getScreenName() );
			editor.commit();
			Log.d(TAG , "store ID and ScreenName.");
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	/**
	 * return current user's twitter id
	 * @param context
	 * @return user id
	 */
	public static long getUserId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences( USER_INFO, Context.MODE_PRIVATE );
		return preferences.getLong(USER_ID, -1);
	}

	/**
	 * @param context
	 * @return user's screen name
	 */
	public static String getUserScreenName(Context context) {
		SharedPreferences preferences = context.getSharedPreferences( USER_INFO, Context.MODE_PRIVATE );
		return preferences.getString(USER_SCREEN_NAME, null);
	}

	public static void removeUser(long userId) {
		//TODO 何とかして，ユーザー情報を消そう
	}
}