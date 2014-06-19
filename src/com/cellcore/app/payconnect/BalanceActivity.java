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
import android.app.AlertDialog.Builder;
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
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class BalanceActivity extends SherlockActivity {
	private EditText accountNumber;
	private ProgressDialog pDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balacelayout);
		
		/**ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle("Check Balance");
		bar.show();**/
		
		
		accountNumber  = (EditText)findViewById(R.id.accountnumber2);
		
		Button proceed  = (Button)findViewById(R.id.proceed2);
		proceed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (!CheckInternetConnection.haveNetworkConnection(BalanceActivity.this)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(BalanceActivity.this);
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
					d.show();return;
				}
				
				
				
				final String number  = accountNumber.getText().toString();
				
				Time now = new Time();
		    	now.setToNow();
		    	
		    	String the_time = now.toString().substring(0, 13);
				
				
				if (validate(new String[] {number})) {
					new GetAccountBalance().execute(number);
					/**Intent intent = new Intent(BalanceActivity.this, ValidationActivity.class);
					Bundle bdl = new Bundle();
					bdl.putString("name", name);
					bdl.putString("number", number);
					bdl.putString("pnumber", pnumber);
					bdl.putString("timestamp", the_time);
					bdl.putInt("type", 2);
					
					intent.putExtras(bdl);
					startActivity(intent);**/
					
				}
				else
					Toast.makeText(getBaseContext(), "Some entries are missing. Fill the above form correctly", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private class GetAccountBalance extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(BalanceActivity.this);
			pDialog.setTitle("PayCollect");
			pDialog.setMessage("getting account info...");
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
		    	nameValuePairs.add(new BasicNameValuePair("mac", Utilities.getSerial()));
		    	
		    	nameValuePairs.add(new BasicNameValuePair("typ","Bal"));
		    	
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
		    	Log.e("error (balance activity)", e.getMessage());
		    	e.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	Log.e("error2 (balance activity) ", e.getMessage());
		    	e.printStackTrace();
		    } catch (Exception exp) {
		    	exp.printStackTrace();
		    }
		   
		    System.out.println("error occurred and result is null");
			return "";
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pDialog.dismiss();
			
			//Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
			
			if (result.equals("")) {Utilities.showDialog(BalanceActivity.this, "Error occurred", "Error occurred while connecting to the internet.").show(); return;}
			
			String message  = "";
			
			try {
				JSONObject object = new JSONObject(result);
				String status  = object.getString("status");
				
				if (status.equals("300")) {
					String accountNum = object.getString("custAccNum");
					String accountBalance = object.getString("custAccBal");
					String accountName = object.getString("custAccNam");
					String phone = object.getString("custPhone");
					String acctType = object.getString("custAccTyp");
					String lastUpdate = object.getString("lastUpdated");
					String bankname  = object.getString("bankName");
					String agentName = object.getString("agentName");
					
					Intent intent = new Intent(BalanceActivity.this, BalanceInfoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("accountName", accountName);
					bundle.putString("accountNumber", accountNum);
					bundle.putString("bal", accountBalance);
					bundle.putString("type", acctType);
					bundle.putString("phone", phone);
					bundle.putString("lastUpdated", lastUpdate);
					bundle.putString("bank", bankname);
					bundle.putString("agent", agentName);
					
					intent.putExtras(bundle);
					
					startActivity(intent);
					finish();
					return;
					
				} else if (status.equals("000"))
					message  = "Invalid AgentID/Password";
				else if (status.equals("001"))
					message  = "AgentID Disabled";
				else if (status.equals("002"))
					message  = "Account already in use";
				else if (status.equals("301"))
					message  = "Balance check Unsuccessful";
				else if (status.equals("401"))
					message = "Invalid Account Number";
				else if (status.equals("502"))
					message = "Transaction not permitted";
				else if (status.equals("701"))
					message = "Transaction limit exceeded";
				
				Utilities.showDialog(BalanceActivity.this, "Error occurred", message).show();
				//Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			System.out.println(result);
		}
		
	}
	
	
	
	private boolean validate (String array[]) {
		for (String s : array)
			if (s.trim().equals(""))
				return false;
		return true;
	}
	
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
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
