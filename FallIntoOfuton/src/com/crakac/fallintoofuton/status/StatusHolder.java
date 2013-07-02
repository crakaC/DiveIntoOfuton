package com.crakac.fallintoofuton.status;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crakac.fallintoofuton.util.TwitterUtils;

/**
 * アクティビティ間でツイート内容(status)を使いまわすためのもの
 * 各アクティビティのonCreate時にContextをセットする．
 * @author Kosuke
 *
 */
@SuppressLint("CommitPrefEdits")
public class StatusHolder {
	private static twitter4j.Status status;
	private static Context mContext;
	private static final String STATUS_ID = "saved_status_id";
	private static final String PREF_NAME = "statusId";
	private static Twitter mTwitter;
	private static final String TAG = StatusHolder.class.getSimpleName();
	
	public static void setContext(Context context){
		mContext = context;
	}
	
	public static void setStatus(twitter4j.Status st)
	{
		status = st;
		
		SharedPreferences preference = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		editor.putLong(STATUS_ID, st.getId());
		editor.commit();
	}
	
	public static twitter4j.Status getStatus(){
		if(status == null){
			SharedPreferences preference = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
			mTwitter = TwitterUtils.getTwitterInstance(mContext);
			try {
				status = mTwitter.showStatus(preference.getLong(STATUS_ID, 0));
				Log.i(TAG, "load status from Twitter");
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		}
		return status;
	}

	public static boolean hasStatus() {
		return status != null;
	}
}
