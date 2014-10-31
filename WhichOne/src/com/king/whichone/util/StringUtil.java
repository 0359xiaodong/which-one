package com.king.whichone.util;

public class StringUtil {
	public static boolean isEmpty(String str) {
		if (str == null || str.equalsIgnoreCase("")) {
			return true;
		} else
			return false;
	}
}
