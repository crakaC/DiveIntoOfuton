package com.crakac.fallintoofuton.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBAdapter {
	static final String DATABASE_NAME = "acount.db";
	static final int DATABASE_VERSION = 1;
	
	public static final String USER_TABLE = "users";
	public static final String LIST_TABLE = "lists";
	
	public static final String COL_USERID = "UserID";

	public static final String COL_SCREEN_NAME = "ScreenName";
	public static final String COL_ICON_URL = "IconURL";
	public static final String COL_TOKEN = "Token";
	public static final String COL_TOKEN_SECRET = "TokenSecret";
	public static final String COL_IS_CURRENT = "IsCurrent";

	public static final String COL_LIST_ID = "ListID";
	public static final String COL_LIST_NAME = "Name";
	public static final String COL_LIST_LONGNAME = "FullName";
	
	protected final Context context;
	protected DatabaseHelper dbHelper;
	protected SQLiteDatabase db;
	
	public UserDBAdapter(Context context){
		this.context = context;
		dbHelper = new DatabaseHelper(this.context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper{
		Context mContext;
		public DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			try{
				execSql(db, "sql/create");
			} catch (IOException e){
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try{
				execSql(db,"sql/drop" );
			} catch (IOException e){
				e.printStackTrace();
			}
			onCreate(db);
		}
		
		/**
		 * 引数に指定したassets内のsqlを実行する
		 * @param db
		 * @param assetsDir
		 */
		private void execSql(SQLiteDatabase db, String assetsDir) throws IOException {
			AssetManager as = mContext.getResources().getAssets();
			try{
				String files[] = as.list(assetsDir);
				for(int i = 0; i < files.length; i++){
					String str = readFile(as.open(assetsDir+"/"+files[i]));
					for(String sql : str.split("/")){
						db.execSQL(sql);
					}
				}
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
		/**
		 * ファイルから文字列を読み込む
		 * @param is
		 * @return ファイルの文字列
		 * @throws IOException
		 */
		private String readFile(InputStream is) throws IOException{
			BufferedReader br = null;
			try{
				br = new BufferedReader(new InputStreamReader(is));
				
				StringBuilder sb = new StringBuilder();
				String str;
				while((str = br.readLine())!=null){
					sb.append(str + "\n");
				}
				return sb.toString();
			} finally {
				if(br!=null){
					br.close();
				}
			}
		}
	}

	public UserDBAdapter open() {
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		dbHelper.close();
	}
	
	public Cursor getAllUsers(){
		return db.query(USER_TABLE, null, null, null, null, null, null);
	}

	public Cursor getLists(long userId){
		String selection ="UserID = ?";
		String[] selectionArgs = { String.valueOf(userId) };
		return db.query(LIST_TABLE, null, selection, selectionArgs, null, null, null);
	}
	
	public boolean deleteUser(long userId){
		return db.delete(USER_TABLE, COL_USERID + "=" + userId, null ) > 0;
	}

	public boolean deleteList(int listId){
		return db.delete(LIST_TABLE, COL_LIST_ID + "=" + listId, null) > 0;
	}
	
	public boolean deleteList(long userId){
		return db.delete(LIST_TABLE, COL_USERID + "=" + userId, null) > 0;		
	}

	/**
	 * データベースにユーザーを追加する．すでにUserIDがある場合は削除して再追加する
	 * @param user
	 */
	public void saveUser(User user){
		deleteUser(user.getUserId());
		ContentValues values = new ContentValues();
		values.put(COL_USERID, user.getUserId());
		values.put(COL_SCREEN_NAME, user.getScreenName());
		values.put(COL_ICON_URL, user.getIconUrl());
		values.put(COL_TOKEN, user.getToken());
		values.put(COL_TOKEN_SECRET, user.getTokenSecret());		
		db.insertOrThrow(USER_TABLE, null, values);
	}
	public void saveList(TwitterList list){
		deleteList(list.getListId());
		ContentValues values = new ContentValues();
		values.put(COL_USERID, list.getUserId());
		values.put(COL_LIST_ID, list.getListId());
		values.put(COL_LIST_NAME, list.getName());
		values.put(COL_LIST_LONGNAME, list.getFullName());
		db.insertOrThrow(LIST_TABLE, null, values);
	}
	
	public void setCurrentUser(User user) {
		//前回までのcurrent userをただのuserにする
		ContentValues values = new ContentValues();
		values.put(COL_IS_CURRENT, -1);
		db.update(USER_TABLE, values, COL_IS_CURRENT + "=1", null);
		
		values = new ContentValues();
		values.put(COL_IS_CURRENT, 1);
		db.update(USER_TABLE, values, COL_USERID + "=" +user.getUserId(), null);
	}
	public Cursor getCurrentUser(){
		return db.query(USER_TABLE, null, COL_IS_CURRENT+"=1", null, null, null, null);
		//COL_IS_CURRENTは-1でfalse, 1がtrueってことにしてる
	}
}
