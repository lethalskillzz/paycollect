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
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class CreditActivity extends SherlockActivity {
	private EditText accountNumber, amount;
	private int type;
	private ProgressDialog pDialog;
	private RadioGroup radioGroup;
	EditText edittext;
	
	String chequenum; //holds the checknumber
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.creditlayout);
		
		
		
		//accountName = (EditText)findViewById(R.id.accountname);
		accountNumber  = (EditText)findViewById(R.id.accountnumber);
		amount = (EditText)findViewById(R.id.amount);
		radioGroup = (RadioGroup)findViewById(R.id.radiocurrency);
		final TextView ctext = (TextView)findViewById(R.id.chequenumbertext);
		edittext = (EditText)findViewById(R.id.chequenumberedittext);
		
		
		final RadioButton cheque = (RadioButton)findViewById(R.id.radiocheque);
		cheque.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (cheque.isChecked()) {
					ctext.setVisibility(View.VISIBLE);
					edittext.setVisibility(View.VISIBLE);
				}
			}
		});
		
		final RadioButton cash = (RadioButton)findViewById(R.id.radiocash);
		cash.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cash.isChecked()) {
					ctext.setVisibility(View.INVISIBLE);
					edittext.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		
		Button button = (Button) findViewById(R.id.proceed);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//String name = accountName.getText().toString();
				
				String number  = accountNumber.getText().toString();
				String amountt = amount.getText().toString();
				
				Time now = new Time();
		    	now.setToNow();
		    	
		    	String the_time = now.toString().substring(0, 13);
				
				
				if (validate(new String[] {number, amountt}) && checkCheque()) {
					
					if (CheckInternetConnection.haveNetworkConnection(CreditActivity.this))
						new FetchCustomerDetails().execute(new String[]{number, amountt, the_time});
					else {
						AlertDialog.Builder builder = new AlertDialog.Builder(CreditActivity.this);
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
					
				}
				else
					Toast.makeText(getBaseContext(), "Some entries are missing. Fill the above form correctly", Toast.LENGTH_SHORT).show();
			}
		});	
		
	}
	
	//this method validates the inputs entered by the user
	private boolean validate (String array[]) {
		for (String s : array)
			if (s.trim().equals(""))
				return false;
		return true;
	}
	
	private boolean checkCheque() {
		int selectedid = radioGroup.getCheckedRadioButtonId();
		if (selectedid != R.id.radiocheque) return true;
		
		if (selectedid == R.id.radiocheque) {
			chequenum = edittext.getText().toString();
			if (chequenum.trim().length() != 0 && !chequenum.equals("")) return true;
		}
		
		return false;
	}
	
	private class FetchCustomerDetails extends AsyncTask<String, String, String[]> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(CreditActivity.this);
			pDialog.setTitle("PayCollect");
			pDialog.setMessage("processing...");
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setCancelable(false);
			pDialog.setIndeterminate(true);
			pDialog.show();
		}

		@Override
		protected String[] doInBackground(String... params) {
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
		    	
		    	nameValuePairs.add(new BasicNameValuePair("typ","Chk"));
		    	
		    	nameValuePairs.add(new BasicNameValuePair("tim", params[2]));
		    	nameValuePairs.add(new BasicNameValuePair("mix", md5(agentid+params[2])));
		    	
		    	//this lines gets whether it is a cash or a cheque transaction
		    	int selectedid = radioGroup.getCheckedRadioButtonId();
		    	String type="";
		    	if (selectedid == R.id.radiocash) {
		    		type = "CASH";
		    		System.out.println("type is cash");
		    	} else if (selectedid == R.id.radiocheque) {
		    		type = "CHEQUE";
		    		System.out.println("type is cheque");
		    		nameValuePairs.add(new BasicNameValuePair("dsc",type));
		    		nameValuePairs.add(new BasicNameValuePair("chequenum",chequenum));
		    	}
		    	
		    	nameValuePairs.add(new BasicNameValuePair("cat",type));
		    	
		    	System.out.println("second type is: "+type);
		    	
		    	
		    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    	
		    	// Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        String data = EntityUtils.toString(response.getEntity());
		        
		        try {
		        	JSONObject object = new JSONObject(data);
		        	data = object.getString("status");
		        } catch (JSONException exp) {
		        	exp.printStackTrace();
		        }
		        
		        return new String[] {data, params[2], params[1], type};
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    	Log.e("error", e.getMessage());
		    	e.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	Log.e("error", e.getMessage());
		    	e.printStackTrace();
		    } catch (Exception e){
		    	Log.e("error", e.getMessage());
		    	e.printStackTrace();
		    }
		    
		    return null;
		}
		
		@Override
		protected void onPostExecute(String result[]) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pDialog.dismiss();
			
			if (result  == null) {
				Utilities.showDialog(CreditActivity.this, "Error occurred", "Error occurred while connecting to the internet.").show();
				return;
			}
			
			Log.d("the returned result is", result[0]);
			
			//Toast.makeText(getBaseContext(), result[0], Toast.LENGTH_LONG).show();
			
			String message = "";
			
			if (result[0].equals("401"))
				message = "Account Number does not exists";
			else if (result[0].equals("000"))
				message = "Invalid agentid/password";
			else if (result[0].equals("002"))
				message = "Agent ID in use";
			else if (result[0].equals("701"))
				message = "Transaction limit exceeded";
			else if (result[0].equals("999"))
				message = "Invalid transaction";
			else {
				try {
					JSONObject array = new JSONObject(result[0]);
					String acctNum = array.getString("custAccNum");
					String acctName = array.getString("custAccNam");
					String phonenum = array.getString("custPhone");
					String bankName = array.getString("bankName");
					String agentName = array.getString("agentName");
					//String transDate = array.getString("transDt");
					
					
					Intent intent = new Intent(CreditActivity.this, ValidationActivity.class);
					Bundle bdl = new Bundle();
					bdl.putString("name", acctName);
					bdl.putString("number", acctNum);
					bdl.putString("pnumber", phonenum);
					bdl.putString("amount", result[2]);
					bdl.putString("timestamp", result[1]);
					bdl.putInt("type", 1);
					bdl.putString("bankName", bankName);
					bdl.putString("agentName", agentName);
					bdl.putString("type2", result[3]);
					
					intent.putExtras(bdl);
					startActivity(intent);
					
				} catch (JSONException je) {
					message = "error occured: " + result[0] + je.getMessage() + " " + result;
					je.printStackTrace();
				}
			}
			
			//means that an error occurred and the error dialog should be displayed
			if (!message.equals("")) {
				Utilities.showDialog(CreditActivity.this, "Error occurred", message).show();
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
