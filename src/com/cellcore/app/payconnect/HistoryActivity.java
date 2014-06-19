package com.cellcore.app.payconnect;

import java.io.IOException;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class HistoryActivity extends SherlockActivity {
	private ProgressDialog pDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		
		final TextView accnum = (TextView) findViewById(R.id.accountnumber2);
		
		Button button = (Button)findViewById(R.id.proceed2);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!CheckInternetConnection.haveNetworkConnection(HistoryActivity.this)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
					builder.setTitle("Internet connection");
					builder.setMessage("No internet connection detected on the device. Please check the connection and try again.");
					builder.setCancelable(true);
					builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					});
					
					builder.setIcon(R.drawable.noconnection);
					
					builder.create().show();
					return;
				}
				
				String num = accnum.getText().toString();
				
				if (num.trim().equals(""))
					Toast.makeText(getBaseContext(), "Account number field is empty.", Toast.LENGTH_LONG).show();
				else {
					new PullHistory().execute(num);
				}
			}
		});
	}
	
	private class PullHistory extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(HistoryActivity.this);
			pDialog.setTitle("PayCollect");
			pDialog.setMessage("history processing...");
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setCancelable(false);
			pDialog.setIndeterminate(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String agentid  = prefs.getString("id", "");
			String password = prefs.getString("password", "");
			
			String url = "http://x.mobileplus.com.ng/paycollect/bep.php";
			
			HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(url);
		    
		    Time now = new Time();
	    	now.setToNow();
	    	
	    	String the_time = now.toString().substring(0, 13);
		    
		    try {
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("usr", agentid));
		    	nameValuePairs.add(new BasicNameValuePair("pwd", md5(password)));
		    	nameValuePairs.add(new BasicNameValuePair("acc", params[0]));
		    	nameValuePairs.add(new BasicNameValuePair("mac", Utilities.getSerial()));
		    	
		    	nameValuePairs.add(new BasicNameValuePair("typ","Trx"));
		    	
		    	nameValuePairs.add(new BasicNameValuePair("tim", the_time));
		    	nameValuePairs.add(new BasicNameValuePair("mix", md5(agentid+the_time)));
		    	
		    	
		    	
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
		    } catch (Exception exp) {exp.printStackTrace();}
		    
			return "";
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pDialog.dismiss();
			
			if (result.equals("")) {
				Utilities.showDialog(HistoryActivity.this, "Error occurred", "Error occurred while connecting to the internet").show();
				return;
			}
			
			System.out.println(result);
			
			try {
				JSONArray array = new JSONArray(result);
				
				Intent intent = new Intent(HistoryActivity.this, HistoryListActivity.class);
				intent.putExtra("values", result);
				startActivity(intent);
				
			} catch (JSONException je) {
				try {
					JSONObject object = new JSONObject(result);
					String status = object.getString("status");
					
					String message = "";
					
					if (status.equals("002"))
						message = "Agent ID is in use";
					else if (status.equals("501"))
						message = "No transactions found.";
					else if (status.equals("401"))
						message =  "Invalid accout number.";
					else if (status.equals("701"))
						message = "Transactions limit exceeded.";
					else if (status.equals("502"))
						message = "Transaction not permitted";
					else
						message = "Error occurred " + status;
					
					Utilities.showDialog(HistoryActivity.this, "Error occurred", message).show();
				} catch (JSONException jee) {
					jee.printStackTrace();
					Utilities.showDialog(HistoryActivity.this, "Error occurred", jee.getMessage()).show();
				}
			}
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
