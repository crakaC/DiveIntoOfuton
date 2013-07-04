package com.crakac.ofuton.lists;

/**
 * リストに変更があったかどうか監視するやつ．
 * メインアクティビティのリロードに使う．
 * @author Kosuke
 *
 */
public class ListObserver {
	private static boolean flag = false;

	public static boolean isChanged() {
		return flag;
	}
	
	public static void init(){
		flag = false;
	}

	public static void changed() {
		flag = true;
	}

	public static void notChanged() {
		flag = false;
	}
}
