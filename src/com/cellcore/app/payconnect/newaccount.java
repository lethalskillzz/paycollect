package com.cellcore.app.payconnect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class newaccount extends SherlockActivity{
	TextView firstname, lastname, address, number, email;
	ProgressDialog pDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newaccount);
		
		firstname = (TextView)findViewById(R.id.firstname);
		lastname = (TextView)findViewById(R.id.lastname);
		address = (TextView)findViewById(R.id.address);
		number = (TextView)findViewById(R.id.mobilenumber);
		email = (TextView)findViewById(R.id.emailaddress);
		
		Button proceed = (Button) findViewById(R.id.proceed);
		proceed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//check for internet connection
				if (!CheckInternetConnection.haveNetworkConnection(newaccount.this)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(newaccount.this);
					builder.setTitle("Internet connection");
					builder.setMessage("No internet connection detected on the device. Please check the connection and try again.");
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
					d.show();
					return;
				}
				
				String _firstname = firstname.getText().toString();
				String _lastname = lastname.getText().toString();
				String _address = address.getText().toString();
				String _number = number.getText().toString();
				String _email = email.getText().toString();
				
				if (!validate(new String[]{_firstname, _lastname, _address, _number, _email}))
					Utilities.showDialog(getBaseContext(), "Missing fileds", "Some fields are missing");
				else {
					new CreateNewAccount().execute(new String[]{_firstname, _lastname, _address, _number, _email});
				}
			}
		});
	}
	
	private boolean validate (String array[]) {
		for (String s : array)
			if (s.trim().equals(""))
				return false;
		return true;
	}
	
	private class CreateNewAccount extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(newaccount.this);
			pDialog.setTitle("PayCollect");
			pDialog.setMessage("creating new account");
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setCancelable(false);
			pDialog.setIndeterminate(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String agentid  = prefs.getString("id", "");
			String password = prefs.getString("password", "");
			
			String name = arg0[0] + " " + arg0[1];
			try {
				name = URLEncoder.encode(name, "utf-8");
			} catch (UnsupportedEncodingException e) {e.printStackTrace();}
			
			String url = "http://x.mobileplus.com.ng/paycollect/bep.php";
			
			HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(url);
		    
		    try {
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("usr", agentid));
		    	nameValuePairs.add(new BasicNameValuePair("pwd", md5(password)));
		    	nameValuePairs.add(new BasicNameValuePair("name", name));
		    	nameValuePairs.add(new BasicNameValuePair("num", arg0[3]));
		    	nameValuePairs.add(new BasicNameValuePair("typ","New"));
		    	nameValuePairs.add(new BasicNameValuePair("adr", arg0[2]));
		    	nameValuePairs.add(new BasicNameValuePair("eml", arg0[4]));
		    	nameValuePairs.add(new BasicNameValuePair("mac", Utilities.getSerial()));
		    	
		    	String time = String.valueOf(System.currentTimeMillis());
		    	
		    	nameValuePairs.add(new BasicNameValuePair("tim", time));
		    	nameValuePairs.add(new BasicNameValuePair("mix", md5(agentid+time)));
		    	
		    	
		    	
		    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    	
		    	// Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        String data = EntityUtils.toString(response.getEntity());
		        
		        
		        return data;
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    	Log.e("error", e.getMessage());
		    	e.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	Log.e("error2", e.getMessage());
		    	e.printStackTrace();
		    }
		    
		    return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pDialog.dismiss();
			
			try {
				JSONObject object = new JSONObject(result);
				String status = object.getString("status");
				
				if (status.equals("600")) {
					String accountnumber = object.getString("AccNum");
					
					AlertDialog.Builder builder = new AlertDialog.Builder(newaccount.this);
					builder.setTitle("Account number");
					builder.setMessage("Your account has been created. \n\nYour new account number is " + accountnumber);
					builder.setCancelable(true); 
					builder.setPositiveButton("close", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
							finish();
						}
					});
					
					builder.setIcon(R.drawable.correct);
					
					AlertDialog d = builder.create();
					d.show();
				} else {
					Utilities.showDialog(getBaseContext(), "Account creation details", "Account could not be created").show();
				}
			} catch (JSONException je) {
				je.printStackTrace();
				Utilities.showDialog(getBaseContext(), "Error occurred", je.getMessage()).show();
			}
			
			//Toast.makeText(getApplication(), result, Toast.LENGTH_LONG).show();
		}
		
	}
	
	public static final String md5(final String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
}
