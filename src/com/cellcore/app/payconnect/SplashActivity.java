package com.cellcore.app.payconnect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;


public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGHT = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!checkIfImageFileExists()) {
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Log.i("image download", "image download started");
							try {
								URL url = new URL("http://x.mobileplus.com.ng/paycollect/images/paycol.bmp");
								HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
								urlConnection.setRequestMethod("GET");
								urlConnection.setDoOutput(true);                   
								urlConnection.connect();
								
								File SDCardRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();
								String filename="paycol.bmp";   
								
								Log.i("Local filename:",""+filename);
								File file = new File(SDCardRoot,filename);
								
								if(file.createNewFile()) {
								    file.createNewFile();
								}  
								
								FileOutputStream fileOutput = new FileOutputStream(file);
								InputStream inputStream = urlConnection.getInputStream();
								int totalSize = urlConnection.getContentLength();
								
								int downloadedSize = 0;   
								byte[] buffer = new byte[1024];
								int bufferLength = 0;
								
								while ( (bufferLength = inputStream.read(buffer)) > 0 ) 
								  {                 
								    fileOutput.write(buffer, 0, bufferLength);                  
								    downloadedSize += bufferLength;                 
								    Log.i("Progress:","downloadedSize:"+downloadedSize+"totalSize:"+ totalSize) ;
								  }             
								  fileOutput.close();
								  if(downloadedSize==totalSize) Log.i("file path is", file.getPath());
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch(IOException ioe) {
								ioe.printStackTrace();
							}
						}
						
					});
					
					t.start();
				}
				Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(mainIntent);
				
				finish();
			}
			
		}, SPLASH_DISPLAY_LENGHT);
		
		
	}
	
	private boolean checkIfImageFileExists() {
		File sdcard = Environment.getExternalStorageDirectory();
		File f1 = new File(sdcard, "paycol.bmp");
			
		if (f1.exists()) return true;
		return false;
	}
	
	
	
	
}
