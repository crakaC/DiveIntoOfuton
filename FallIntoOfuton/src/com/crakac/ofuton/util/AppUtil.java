package com.crakac.ofuton.util;

import android.content.Context;
import android.widget.Toast;

public final class AppUtil {
	public AppUtil() {
	}
	public static void showToast(Context context, String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}
