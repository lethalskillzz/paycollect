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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class WithdrawalActivity extends SherlockActivity {
	TextView accNum, amount, passcode;
	ProgressDialog pDialog;
	private RadioGroup radioGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.withdrawallayout);
		
		accNum = (TextView)findViewById(R.id.waccountnumber);
		amount = (TextView)findViewById(R.id.wamount);
		passcode = (TextView)findViewById(R.id.passcode);
		radioGroup = (RadioGroup)findViewById(R.id.radiogroup);

		Button button = (Button) findViewById(R.id.proceed);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String number = accNum.getText().toString();
				String _amount = amount.getText().toString();
				String code = passcode.getText().toString();
				
				if (validate(new String[]{number, _amount, code})) {
					Time now = new Time();
			    	now.setToNow();
			    	
			    	String the_time = now.toString().substring(0, 13);
			    	
			    	if (CheckInternetConnection.haveNetworkConnection(WithdrawalActivity.this))
						new FetchWithdrawalDetails().execute(new String[]{number, _amount, the_time, code});
					else {
						AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawalActivity.this);
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
					}
			    	
				} else
					Toast.makeText(getBaseContext(), "Some entrioes are missing. Please fill in the fields correctly.", Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private class FetchWithdrawalDetails extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(WithdrawalActivity.this);
			pDialog.setTitle("PayCollect");
			pDialog.setMessage("processing transaction...");
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
		    
		    try {
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("usr", agentid));
		    	nameValuePairs.add(new BasicNameValuePair("pwd", md5(password)));
		    	nameValuePairs.add(new BasicNameValuePair("acc", params[0]));
		    	nameValuePairs.add(new BasicNameValuePair("pin",md5(params[3]+params[2])));
		    	nameValuePairs.add(new BasicNameValuePair("typ","Dbt"));
		    	nameValuePairs.add(new BasicNameValuePair("amt",params[1]));
		    	nameValuePairs.add(new BasicNameValuePair("tim", params[2]));
		    	nameValuePairs.add(new BasicNameValuePair("mix", md5(agentid+params[2])));
		    	nameValuePairs.add(new BasicNameValuePair("mac", Utilities.getSerial()));
		    	
		    	int selectedid = radioGroup.getCheckedRadioButtonId();
		    	if (selectedid == R.id.radiocurrent)
		    		nameValuePairs.add(new BasicNameValuePair("cat", "cur"));
		    	else if (selectedid == R.id.radiosavings)
		    		nameValuePairs.add(new BasicNameValuePair("cat", "sav"));
		    	
		    	
		    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    	
		    	// Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        String data = EntityUtils.toString(response.getEntity());
		        
		        /**try {
		        	JSONObject object = new JSONObject(data);
		        	data = object.getString("status");
		        } catch (JSONException exp) {
		        	exp.printStackTrace();
		        }**/
		        
		        return data;
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    	Log.e("error", e.getMessage());
		    	e.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	Log.e("error2", e.getMessage());
		    	e.printStackTrace();
		    } catch (Exception exp) {
		    	exp.printStackTrace();
		    }
		    
			return "";
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			pDialog.dismiss();
			System.out.println(result);
			
			if (result.equals("")) {
				Utilities.showDialog(WithdrawalActivity.this, "Error occurred", "Error occurred while connecting to the internet").show();
				return;
			}
			
			try {
				JSONObject object = new JSONObject(result);
				String status = object.getString("status");
				
				if (status.equals("999")) {
					Utilities.showDialog(WithdrawalActivity.this, "Error occurred", "Transaction incomplete.").show();
					//Toast.makeText(getBaseContext(), "transaction incomplete.", Toast.LENGTH_LONG).show();
					return;
				} else if (status.equals("202")) {
					Utilities.showDialog(WithdrawalActivity.this, "Error occurred", "Insufficient cash").show();
					//Toast.makeText(getBaseContext(), "insufficient cash", Toast.LENGTH_LONG).show();
					return;
				} else if (status.equals("002")) {
					Utilities.showDialog(WithdrawalActivity.this, "Error occurred", "Agent ID is in use.").show();
					//Toast.makeText(getBaseContext(), "Agent ID is in use.", Toast.LENGTH_LONG).show();
					return;
				} else if (status.equals("701")) {
					Utilities.showDialog(WithdrawalActivity.this, "Error occurred", "Transaction Limit Exceeded").show();
				}
				
				
				String transID = object.getString("transID");
				String transDate = object.getString("transDt");
				String amt = object.getString("transAmt");
				
				Intent intent = new Intent(WithdrawalActivity.this, WinthdrawalInfoActivity.class);
				Bundle extras = new Bundle();
				extras.putString("id", transID);
				extras.putString("date", transDate);
				extras.putString("amount", amt);
				intent.putExtras(extras);
				startActivity(intent);
				
			} catch (JSONException je) {
				je.printStackTrace();
			}
			
			//Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
		}
	}
	
	
	private boolean validate(String array[]) {
		for (String s : array)
			if (s.trim().equals(""))
				return false;
		return true;
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
