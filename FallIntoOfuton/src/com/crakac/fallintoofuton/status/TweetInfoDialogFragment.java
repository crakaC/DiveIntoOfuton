package com.crakac.fallintoofuton.status;

import com.crakac.fallintoofuton.R;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * TweetActivityから，リプライ時にリプライ先のツイート情報を表示するためのDialogFragment
 * @author Kosuke
 *
 */
public class TweetInfoDialogFragment extends DialogFragment {

	private static final String TAG = TweetInfoDialogFragment.class.getSimpleName();
	private TweetStatusAdapter mAdapter;
	private ListView lvStatus;
	private Dialog dialog;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dialog = new Dialog(getActivity());
		// タイトルを消す
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// レイアウトを適用
		dialog.setContentView(R.layout.tweet_info_dialog);
		// ツイート詳細を表示
		lvStatus = (ListView) dialog.findViewById(R.id.tweet_status);
		lvStatus.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				dialog.dismiss();
			}
		});
		mAdapter = new TweetStatusAdapter(getActivity());
		lvStatus.setAdapter(mAdapter);
		setStatusAdapterAsync();
		return dialog;
	}
	private void setStatusAdapterAsync() {
		AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				mAdapter.add(StatusHolder.getStatus());
				mAdapter.notifyDataSetChanged();
				return null;
			}
		};
		task.execute();
	}
}
