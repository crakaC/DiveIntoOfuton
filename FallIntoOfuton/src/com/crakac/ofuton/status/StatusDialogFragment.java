package com.crakac.ofuton.status;

import java.util.ArrayList;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import com.crakac.fallintoofuton.R;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * ツイートをタップした時に出てくるダイアログ
 * 
 * @author Kosuke
 * 
 */
public class StatusDialogFragment extends DialogFragment {

	private StatusActionAdapter mActionAdapter;
	private Dialog dialog;

	public static interface ActionSelectListener {
		void onReply();

		void onFav();

		void onRT();

		void onUser();

		void onLink(String url);

		void onMedia(String url);

		void onHashTag(String tag);

		void onConvesation();
	}

	public StatusDialogFragment() {
	}

	public StatusDialogFragment(ActionSelectListener targetFragment) {
		setTargetFragment((Fragment) targetFragment, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.status_dialog, container);
		// 各種アクションをアダプタに追加して表示
		mActionAdapter = new StatusActionAdapter(getActivity());

		// リストビューを作成
		// ダイアログ全体がひとつのリストビューを含む．
		// 2つ（ツイート表示用，アクション表示用）だと，ツイートが縦に長いと全画面分の領域を使ってしまい，アクションを選択できなくなる
		ListView lvActions = (ListView) view
				.findViewById(R.id.status_action_list);

		// ステータス表示部分を作成．タイムライン中と同じレイアウトなのでTweetStatusAdapter内の処理を使いまわす．
		View statusView = TweetStatusAdapter.createView(
				StatusHolder.getStatus(), null);
		statusView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		// HeaderView（ツイートステータス表示用）をadd. setAdapterより先にしないと落ちる
		lvActions.addHeaderView(statusView);
		// アダプタをセット
		lvActions.setAdapter(mActionAdapter);
		// reply
		mActionAdapter
				.add(new Pair<String, Integer>(null, StatusConstant.REPLY));
		// retweet
		mActionAdapter.add(new Pair<String, Integer>(null,
				StatusConstant.RETWEET));
		// favorite
		mActionAdapter.add(new Pair<String, Integer>(null, StatusConstant.FAV));

		// conversation　RTの場合を考慮
		if (StatusHolder.getStatus().isRetweet()
				&& StatusHolder.getStatus().getRetweetedStatus()
						.getInReplyToScreenName() != null) {
			mActionAdapter.add(new Pair<String, Integer>(null,
					StatusConstant.CONVERSATION));
		} else if (StatusHolder.getStatus().getInReplyToScreenName() != null) {
			mActionAdapter.add(new Pair<String, Integer>(null,
					StatusConstant.CONVERSATION));
		}

		// user, media, url, hashtag entities
		setEntities();
		mActionAdapter.notifyDataSetChanged();

		lvActions.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ActionSelectListener listener = (ActionSelectListener) getTargetFragment();

				ListView lv = (ListView) parent;
				Pair<String, Integer> item = (Pair<String, Integer>) lv
						.getItemAtPosition(position);
				dialog.dismiss();
				int type = item.second;
				if (type == StatusConstant.REPLY) {
					listener.onReply();
				} else if (type == StatusConstant.RETWEET) {
					listener.onRT();
				} else if (type == StatusConstant.FAV) {
					listener.onFav();
				} else if (type == StatusConstant.FAV_AND_RT) {
					listener.onFav();
					listener.onRT();
				} else if (type == StatusConstant.LINK) {
					listener.onLink(item.first);
				} else if (type == StatusConstant.MEDIA) {
					listener.onLink(item.first);
				} else if (type == StatusConstant.HASHTAG) {
					listener.onHashTag(item.first);
				} else if (type == StatusConstant.CONVERSATION) {
					listener.onConvesation();
				}
			}
		});
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        dialog = getDialog();  
        
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
          
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        
        //縦幅はwrap contentで，横幅は92%で．
        int dialogWidth = (int) (metrics.widthPixels * 0.92);  
        //int dialogHeight = (int) (metrics.heightPixels * 1.0);  
          
        lp.width = dialogWidth;
        //lp.height = dialogHeight;
        dialog.getWindow().setAttributes(lp);  
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dialog = new Dialog(getActivity());
		// タイトル部分を消す．消さないとダイアログの表示位置が下にずれる
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		// レイアウトはonCreateViewで作られる．ので，dialog.setContentViewはいらない
		
		//全画面化
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

		//背景を透明に
		dialog.getWindow().setBackgroundDrawable(	new ColorDrawable(Color.TRANSPARENT));
		
		return dialog;
	}

	private void setEntities() {
		twitter4j.Status status = StatusHolder.getStatus();
		twitter4j.Status rtStatus = status.getRetweetedStatus();
		// set user entities
		ArrayList<String> users = new ArrayList<String>();// statusに関係あるscreenNameをかたっぱしから突っ込む(@抜き)
		users.add(status.getUser().getScreenName());// ツイートまたはリツイートした人
		UserMentionEntity[] userMentionEntities = status
				.getUserMentionEntities();
		for (UserMentionEntity user : userMentionEntities) {
			if (!users.contains(user.getScreenName()))
				users.add(user.getScreenName());
		}
		if (status.isRetweet()) {
			UserMentionEntity[] umEntities = rtStatus.getUserMentionEntities();
			for (UserMentionEntity user : umEntities) {
				if (!users.contains(user.getScreenName()))
					users.add(user.getScreenName());
			}
		}
		for (String user : users) {
			mActionAdapter.add(new Pair<String, Integer>("@" + user,
					StatusConstant.USER));
		}

		// set Media entities
		MediaEntity[] mediaEntities;
		ArrayList<String> medias = new ArrayList<String>();
		if (status.isRetweet()) {
			mediaEntities = rtStatus.getMediaEntities();
		} else {
			mediaEntities = status.getMediaEntities();
		}
		for (MediaEntity media : mediaEntities) {
			medias.add(media.getExpandedURL());
			mActionAdapter.add(new Pair<String, Integer>(
					media.getExpandedURL(), StatusConstant.MEDIA));
			Log.d("MediaEntity", media.getExpandedURL());
		}

		// set url entities
		URLEntity[] urlEntities;
		if (status.isRetweet()) {
			urlEntities = rtStatus.getURLEntities();
		} else {
			urlEntities = status.getURLEntities();
		}
		for (URLEntity url : urlEntities) {
			if (!medias.contains(url.getExpandedURL())) {
				mActionAdapter.add(new Pair<String, Integer>(url
						.getExpandedURL(), StatusConstant.LINK));
				Log.d("URLEntity", url.getExpandedURL());
			}
		}

		// set Hashtag entities
		HashtagEntity[] hashtags;
		if (status.isRetweet()) {
			hashtags = rtStatus.getHashtagEntities();
		} else {
			hashtags = status.getHashtagEntities();
		}
		for (HashtagEntity hashtag : hashtags) {
			mActionAdapter.add(new Pair<String, Integer>("#"
					+ hashtag.getText(), StatusConstant.HASHTAG));
		}
	}
}
