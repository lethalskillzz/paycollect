package com.cellcore.app.payconnect;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Utilities {
	public static String getSerial() {
		return android.os.Build.SERIAL;
	}
	
	public static AlertDialog showDialog(Context context, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(true);
		builder.setPositiveButton("close", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
	
		builder.setIcon(R.drawable.noconnection);
		
		AlertDialog d = builder.create();
		return d;
	}
	
	
}
