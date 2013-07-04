package com.crakac.ofuton.lists;

import android.util.Log;

/**
 * リストに変更があったかどうか監視するやつ．
 * メインアクティビティのリロードに使う．
 * @author Kosuke
 *
 */
public class ListObserver {
	private static final String TAG = ListObserver.class.getSimpleName();
	private static boolean flag = false;

	public static boolean isChanged() {
		Log.d(TAG, "isChanged() : " + flag);
		return flag;
	}
	
	public static void init(){
		flag = false;
		Log.d(TAG, "init() : " + flag);
	}

	public static void changed() {
		flag = true;
		Log.d(TAG, "changed() : " + flag);
	}

	public static void notChanged() {
		flag = false;
		Log.d(TAG, "notChanged() : " + flag);
	}
}
