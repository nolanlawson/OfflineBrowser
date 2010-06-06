package com.nolanlawson.offlinebrowser;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	
	public static void showErrorToast(Context context, int stringResourceId, Object... args) {
		String message = String.format(context.getResources().getString(stringResourceId),args);
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
}
