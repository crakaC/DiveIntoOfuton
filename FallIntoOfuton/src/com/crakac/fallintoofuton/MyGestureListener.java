package com.crakac.fallintoofuton;

import com.crakac.fallintoofuton.util.AppUtil;

import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class MyGestureListener extends SimpleOnGestureListener{
	Context mContext;
	public MyGestureListener(Context context) {
		mContext = context;
	}
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		AppUtil.showToast(mContext, "Double Tap");
		return super.onDoubleTap(e);
	}
	@Override
	public void onLongPress(MotionEvent e) {
		AppUtil.showToast(mContext, "Long Press");
		super.onLongPress(e);
	}
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		AppUtil.showToast(mContext, "Single Tap");
		return super.onSingleTapConfirmed(e);
	}
}
