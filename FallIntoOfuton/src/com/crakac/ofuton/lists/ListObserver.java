package com.crakac.ofuton.lists;

/**
 * ���X�g�ɕύX�����������ǂ����Ď������D
 * ���C���A�N�e�B�r�e�B�̃����[�h�Ɏg���D
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
