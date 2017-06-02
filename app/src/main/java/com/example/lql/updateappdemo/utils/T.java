package com.example.lql.updateappdemo.utils;

import android.content.Context;
import android.widget.Toast;

public class T {
	static Toast toast;

	public static void shortToast(Context mContext, String text)
	{
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
		toast = Toast.makeText(mContext, text + "", Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void longToast(Context mContext, String text)
	{
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
		toast = Toast.makeText(mContext, text + "", Toast.LENGTH_LONG);
		toast.show();
	}




}
