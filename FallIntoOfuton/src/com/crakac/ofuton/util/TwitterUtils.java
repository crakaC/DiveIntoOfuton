/**
 * Created by Kosuke on 13/05/18.
 */

package com.crakac.ofuton.util;
import java.util.ArrayList;
import java.util.List;

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
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.WebChromeClient.CustomViewCallback;

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
		
		if( existCurrentUser(context) ){
			User currentUser = getCurrentUser(context);
			twitter.setOAuthAccessToken( new AccessToken(currentUser.getToken(), currentUser.getTokenSecret()) );
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
		
		if( existCurrentUser(context) ){
			User currentUser = getCurrentUser(context);
			twitter.setOAuthAccessToken( new AccessToken(currentUser.getToken(), currentUser.getTokenSecret()) );
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
	 * return current user's twitter id
	 * @param context
	 * @return user id
	 */
	public static long getCurrentUserId(Context context) {
		User user = getCurrentUser(context);
		if(user != null){
			return user.getUserId();
		} else {
			return -1;
		}
	}

	public static void removeUser(User item) {
		//TODO 何とかして，ユーザー情報を消そう
	}

	public static void addUser(Context context){
		Twitter tw = getTwitterInstanceForAuth(context);
		tw.setOAuthAccessToken(loadAccessToken(context));
		UserDBAdapter dbAdapter = new UserDBAdapter(context);
		dbAdapter.open();
		twitter4j.User user;
		try {
			user = tw.showUser(tw.getId());
			dbAdapter.saveUser(new User(
					user.getId(),
					user.getScreenName(), 
					user.getProfileImageURL(),
					loadAccessToken(context).getToken(),
					loadAccessToken(context).getTokenSecret(),
					false));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		dbAdapter.close();
	}
	public static List<User> getUsers(Context context){
		List<User> users = new ArrayList<User>();
		UserDBAdapter dbAdapter = new UserDBAdapter(context);
		dbAdapter.open();
		Cursor c = dbAdapter.getAllUsers();
		while(c.moveToNext()){
			users.add(new User(
					c.getLong(c.getColumnIndex(UserDBAdapter.COL_USERID)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_SCREEN_NAME)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_ICON_URL)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_TOKEN)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_TOKEN_SECRET)),
					c.getInt(c.getColumnIndex(UserDBAdapter.COL_IS_CURRENT)) > 0
					));
		}
		dbAdapter.close();
		return users;
	}

	public static void addList(Context context, TwitterList list) {
		UserDBAdapter dbAdapter = new UserDBAdapter(context);
		dbAdapter.open();
		dbAdapter.saveList(list);
		dbAdapter.close();
	}

	public static List<TwitterList> getLists(Context context) {
		List<TwitterList> list = new ArrayList<TwitterList>();
		UserDBAdapter dbAdapter = new UserDBAdapter(context);
		dbAdapter.open();
		Cursor c = dbAdapter.getLists(getCurrentUser(context).getUserId());
		while(c.moveToNext()){
			list.add(new TwitterList(
					c.getLong(c.getColumnIndex(UserDBAdapter.COL_USERID)),
					c.getInt(c.getColumnIndex(UserDBAdapter.COL_LIST_ID)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_LIST_NAME)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_LIST_LONGNAME))
					));
		}
		dbAdapter.close();//cursorを使用するより先にcloseするとinvalid statement in fillWindow()とかいうエラーが出る
		return list;
	}

	public static void setCurrentUser(Context context, User user) {
		//DB上に情報を保存
		UserDBAdapter dbAdapter = new UserDBAdapter(context);
		dbAdapter.open();
		dbAdapter.setCurrentUser(user);
		dbAdapter.close();
	}

	public static User getCurrentUser(Context context){
		User user = null;
		UserDBAdapter dbAdapter = new UserDBAdapter(context);
		dbAdapter.open();
		Cursor c = dbAdapter.getCurrentUser();
		if(c.moveToFirst()){
			user = new User(
					c.getLong(c.getColumnIndex(UserDBAdapter.COL_USERID)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_SCREEN_NAME)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_ICON_URL)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_TOKEN)),
					c.getString(c.getColumnIndex(UserDBAdapter.COL_TOKEN_SECRET)),
					c.getInt(c.getColumnIndex(UserDBAdapter.COL_IS_CURRENT)) > 0
			);
		}
		dbAdapter.close();
		return user;
	}

	public static boolean existCurrentUser(Context context) {
		return getCurrentUser(context) != null;
	}
}