package com.crakac.ofuton;

import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.status.StatusHolder;
import com.crakac.ofuton.status.TweetInfoDialogFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.Twitter;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class TweetActivity extends FragmentActivity {

	private EditText mInputText;
	// private AsyncTwitter mTwitter;
	private Twitter mTwitter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StatusHolder.setContext(this);
		final Intent intent = getIntent();// reply時に必要なデータを持ってくる
		final long replyId = intent.getLongExtra("replyId", -1);// reply先ID
		final String replyName = intent.getStringExtra("replyName");
		final String hashTag = intent.getStringExtra("hashTag");
		setContentView(R.layout.activity_tweet);
		mTwitter = TwitterUtils.getTwitterInstance(this);
		mInputText = (EditText) findViewById(R.id.input_text);
		Button btn = (Button) findViewById(R.id.action_tweet);
		ImageView infoBtn = (ImageView) findViewById(R.id.tweetInfoBtn);

		if (replyName != null && replyId != -1) {
			mInputText.setText("@" + replyName + " ");
			mInputText.setSelection(mInputText.getText().toString().length());
//			if (!StatusHolder.hasStatus()) {
//				try {
//					StatusHolder.setStatus(mTwitter.showStatus(replyId));
//				} catch (TwitterException e) {
//					e.printStackTrace();
//				}
//			}
			infoBtn.setVisibility(View.VISIBLE);
			infoBtn.setOnClickListener(new View.OnClickListener() {
				AsyncTask<Void,Void,Void> task;
				@Override
				public void onClick(View v) {
					if(task!=null && task.getStatus() == AsyncTask.Status.RUNNING){
						return;
					}
					task = new AsyncTask<Void, Void, Void>() {
						
						@Override
						protected Void doInBackground(Void... params) {
							new TweetInfoDialogFragment().show(
									getSupportFragmentManager(), "StatusInfo");
							return null;
						}
					};
					task.execute();
				}
			});
		}
		if(hashTag != null){
			mInputText.setText(mInputText.getText().toString()+" "+hashTag);
		}

		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StatusUpdate update = new StatusUpdate(mInputText.getText()
						.toString());
				if (replyId > 0) {
					update.setInReplyToStatusId(replyId);
				}
				updateStatus(update);
				finish();
			}
			private void updateStatus(StatusUpdate update) {
				AsyncTask<twitter4j.StatusUpdate, Void, twitter4j.Status> task = new AsyncTask<twitter4j.StatusUpdate, Void, twitter4j.Status>() {
					@Override
					protected twitter4j.Status doInBackground(
							twitter4j.StatusUpdate... params) {
						twitter4j.Status status = null;
						try {
							status = mTwitter.updateStatus(params[0]);
						} catch (TwitterException e) {
							e.printStackTrace();
						}
						return status;
					}
					@Override
					protected void onPostExecute(twitter4j.Status result) {
						if (result == null) {
							AppUtil.showToast(getApplicationContext(), "ツイート失敗");
						} else {
							AppUtil.showToast(getApplicationContext(), "つぶやきました");
						}
					}
				};
				task.execute(update);
			}
		});
	}
}
