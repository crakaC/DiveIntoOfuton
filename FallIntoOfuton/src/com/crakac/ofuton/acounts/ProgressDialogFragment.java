package com.crakac.ofuton.acounts;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Window;

public class ProgressDialogFragment extends DialogFragment{
	public static ProgressDialogFragment newInstance(String msg){
		ProgressDialogFragment dialog = new ProgressDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("msg", msg);
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setMessage(getArguments().getString("msg"));
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		return dialog;
	}
	
}
