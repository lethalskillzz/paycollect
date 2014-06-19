package com.cellcore.app.payconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class ValidationActivity extends SherlockActivity {
	private TextView name, amount, pnumber, accnumber, bankName;
	
	private ProgressDialog pDialog;
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mBluetoothSocket;
	private BluetoothDevice mDevice;
	
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private Thread workerThread;
	
	byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
	
	String timestamp = "";
	String agentid;
	String password , agentName,  tdate = "", tID = "";
	
	String acName,_amount, phone, _acNumber, type2;
	
	
	int type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.validationlayout);
		
		Bundle extras = getIntent().getExtras();
		
		acName = extras.getString("name");
		_amount = extras.getString("amount");
		phone = extras.getString("pnumber");
		_acNumber = extras.getString("number");
		type  = extras.getInt("type");
		final String bankname = extras.getString("bankName");
		agentName = extras.getString("agentName");
		type2 = extras.getString("type2");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		agentid  = prefs.getString("id", "");
		password = prefs.getString("password", "");
		
		java.util.Date date= new java.util.Date();
		timestamp = new Timestamp(date.getTime()).toString(); 
		
		name = (TextView) findViewById(R.id.accountname1);
		amount = (TextView) findViewById(R.id.amount1);
		pnumber = (TextView) findViewById(R.id.phonenumber1);
		accnumber = (TextView)findViewById(R.id.accountnumber1);
		bankName = (TextView)findViewById(R.id.thebankname);
		
		name.setText(acName);
		amount.setText(_amount);
		pnumber.setText(phone);
		accnumber.setText(_acNumber);
		bankName.setText(bankname);
		
		Button back = (Button)findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		Button submit = (Button)findViewById(R.id.submit);
		submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (CheckInternetConnection.haveNetworkConnection(ValidationActivity.this)) {
					findBluetoothDevice();
					//new PostDetails().execute(new String[]{acName, _amount, phone, _acNumber});
				} else {
					AlertDialog dialog = new AlertDialog.Builder(ValidationActivity.this).create();
					dialog.setTitle("Internet connection");
					dialog.setMessage("No internet connection detected on the device. Please check the connection and try again.");
					dialog.setCancelable(true);
					dialog.setIcon(R.drawable.noconnection);
					dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "close", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					});
					
					dialog.show();
					
				}
				
			}
		});
	}
	
class PostDetails extends AsyncTask<String, String, String> {
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(ValidationActivity.this);
			pDialog.setTitle("PayCollect");
			pDialog.setMessage("submitting...");
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setCancelable(true);
			pDialog.setIndeterminate(true);
			pDialog.show();
			
		    
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			
			
			String url = "http://x.mobileplus.com.ng/paycollect/bep.php";
			//?usr=agentid&pwd=password&acc=account_number&nam=account_name&typ=Cr_or_Dr&amt=amount";
			
			HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(url);
		    
		    try {
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("usr", agentid));
		    	nameValuePairs.add(new BasicNameValuePair("pwd", md5(password)));
		    	nameValuePairs.add(new BasicNameValuePair("acc", params[3]));
		    	nameValuePairs.add(new BasicNameValuePair("nam", params[0]));
		    	nameValuePairs.add(new BasicNameValuePair("num", params[2]));
		    	nameValuePairs.add(new BasicNameValuePair("mac", Utilities.getSerial()));
		    	
		    	if (type == 1) {
		    		nameValuePairs.add(new BasicNameValuePair("typ","Crd"));
		    		nameValuePairs.add(new BasicNameValuePair("amt", params[1]));
		    	}
		    	else {
		    		nameValuePairs.add(new BasicNameValuePair("typ","Bal"));
		    		nameValuePairs.add(new BasicNameValuePair("amt", "0.0"));
		    	}
		    	
		    	
		    	/**Time now = new Time();
		    	now.setToNow();
		    	
		    	String the_time = now.toString().substring(0, 13);**/
		    	
		    	nameValuePairs.add(new BasicNameValuePair("tim", timestamp));
		    	nameValuePairs.add(new BasicNameValuePair("mix", md5(agentid+timestamp)));
		    	nameValuePairs.add(new BasicNameValuePair("cat", type2));
		    	
		    	//Log.i("now time is", the_time);
		    	
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
		    	
		    } catch (Exception exp) {
		    	exp.printStackTrace();
		    }
		    
		    closeConnection();
			return "";
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pDialog.dismiss();
			
			if (result.equals("")){
				Utilities.showDialog(ValidationActivity.this, "Error occurred", "Error occurred while connecting to the internet.").show();
				return;}
			
			Log.i("result is", result);
			String status  = "";
			
			
			try {
				JSONObject object = new JSONObject(result);
				status = object.getString("status");
				tdate = object.getString("transDt");
				tID = object.getString("transID");
				
			} catch (JSONException ioe) {
				ioe.printStackTrace();
				Toast.makeText(getBaseContext(), result + "\n" + ioe.getMessage(), Toast.LENGTH_LONG).show();
				closeConnection();
			}
			
			
			
			if (status.equals("100")) {
				try {
					//findBluetoothDevice();
					if (sendDataTobePrinted(tdate, tID))
						if (sendDataTobePrinted(tdate, tID))
							closeConnection();
						else
							closeConnection();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					closeConnection();
				}
				/**new AsyncTask<Void, Void, Void>() {
					protected void onPreExecute() {
						pDialog.setTitle("PayCollect");
						pDialog.setMessage("printing...");
						pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						pDialog.setCancelable(true);
						pDialog.setIndeterminate(true);
						pDialog.show();
					}

					@Override
					protected Void doInBackground(Void... params) {
						// TODO Auto-generated method stub
						try {
							sendDataTobePrinted(tdate, tID);
						} catch (IOException ioe) {
							System.out.println("Error inside the button onclick method");
							ioe.printStackTrace();
						}
						return null;
					}
					
					protected void onPostExecute(Void result) {
						pDialog.dismiss();
						Intent intent = new Intent(ValidationActivity.this, TransactionOptions.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
						Toast.makeText(getBaseContext(), "Transaction Successful ", Toast.LENGTH_LONG).show();
					}
					
				}.execute();**/
				
				
				
				
			} else {
				String message = "";
				if (status.equals("000"))
					message = "Invalid AgentID/Password";
				else if (status.equals("001"))
					message  = "AgentID Disabled";
				else if (status.equals("002"))
					message  = "Agent ID in use";
				else if (status.equals("101"))
					message = "Credit transaction unsuccessful";
				else if (status.equals("200"))
					message  = "Debit Transaction Successful";
				else if (status.equals("201"))
					message  = "Debit Transaction unsuccessful";
				else if (status.equals("300"))
					message  = "Balance request accepted. Result will be available shortly";
				else if (status.equals("301"))
					message  = "Balnce request not successful";
				else if (status.equals("999"))
					message = "Transaction Incomplete";
				else if (status.equals("701"))
					message = "Transaction Limit Exceeded";
				
				if (!message.equals("")) {
					Utilities.showDialog(ValidationActivity.this, "Error occurred 1 " , message + " " + status).show();
					
					System.out.println("message not equals ran");
				}
				else {
					Utilities.showDialog(ValidationActivity.this, "Error occurred 2 ", message + " " + status).show();
					System.out.println("reuslt printing ran");
				}
				
				closeConnection();
			}
			
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				System.out.println("result is okay");
				
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device  : pairedDevices) {
						if (device.getName().equals("MP300")) {
							mDevice = device;
							break;
						}
					}
				}
				
				
				try {
					openConnectionToBluetoothPrinter();
					Toast.makeText(getBaseContext(), "Bluetooth Device found", Toast.LENGTH_LONG).show();
					
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	private void findBluetoothDevice() {
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Toast.makeText(getBaseContext(), "No bluetooth adapter found", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetooth, 0);
				
			} else {
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device  : pairedDevices) {
						if (device.getName().equals("MP300")) {
							mDevice = device;
							break;
						}
					}
				}
				
				try {
					openConnectionToBluetoothPrinter();
				} catch (IOException ioe) {
					ioe.printStackTrace();
					Toast.makeText(getBaseContext(), ioe.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
			
			/**Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device  : pairedDevices) {
					if (device.getName().equals("MP300")) {
						mDevice = device;
						break;
					}
				}
			}
			
			Toast.makeText(getBaseContext(), "Bluetooth Device found", Toast.LENGTH_LONG).show();**/
		} catch (NullPointerException e) {
			System.out.println("in the findBluetoothAdapter method. The Catch null pointer exception is running");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("in the findBluetoothAdapter method. The Catch Exception exception is running");
			e.printStackTrace();
		}
	}
	
	private void openConnectionToBluetoothPrinter() throws IOException {
		try {
			System.out.println("in the method that is suppose to open connection");
			
			UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
			mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
			mBluetoothSocket.connect();
			mOutputStream = mBluetoothSocket.getOutputStream();
			mInputStream = mBluetoothSocket.getInputStream();
			
			beginListenForData();
			
			Toast.makeText(getBaseContext(), "Bluetooth Connection opened", Toast.LENGTH_LONG).show();
			
			new PostDetails().execute(new String[]{acName, _amount, phone, _acNumber});
			
			//sendDataTobePrinted(tdate, tID);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			Utilities.showDialog(ValidationActivity.this, "Printer Error" ,ioe.getMessage()+"\nPlease restart or power on the bluetooth printer.").show();
			
			//Toast.makeText(getBaseContext(), ioe.getMessage() + "\nPlease restart ", Toast.LENGTH_LONG).show();
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private boolean sendDataTobePrinted(String tdate, String tid) throws IOException {
		try {
			mOutputStream.write("--------------------------------\n".getBytes());
			new PrintImage().printTheImage(ValidationActivity.this, mOutputStream, "paycol.bmp");
			String msg  = "";
			msg += bankName.getText().toString();
			msg += "\nTrnxnID: " + tid;
			msg += "\nDate: " + tdate;
			msg += "\nAccName: " + name.getText().toString()+"\n" ;
			msg += "AccNumb: " + accnumber.getText().toString() +"\n";
			msg += "Amount: =N="+ amount.getText().toString()+ " Credited\n";
			msg += "PhoneNo: "+ pnumber.getText().toString();
			msg += "\nDepositor: " + agentName;
			msg += "\n--------------------------------\n";
			
			mOutputStream.write(msg.getBytes());
			mOutputStream.write("\n".getBytes());
			
			
			finish();
			Toast.makeText(getBaseContext(), "Transaction Successful ", Toast.LENGTH_LONG).show();
			return true;
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void closeConnection() {
		try {
			stopWorker = true;
			mOutputStream.close();
			mInputStream.close();
			mBluetoothSocket.close();
			Toast.makeText(getBaseContext(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
		} catch (NullPointerException e)  {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	/*
     * After opening a connection to bluetooth printer device, 
     * we have to listen and check if a data were sent to be printed.
     */
	
	private void beginListenForData() {
		try {
			final Handler handler = new Handler();
			
			// This is the ASCII code for a newline character
            final byte delimiter = 10;
            
            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            
            workerThread = new Thread(new Runnable() {
            	public void run() {
            		while (!Thread.currentThread().isInterrupted() && !stopWorker) {
            			try {
            				int bytesAvailable = mInputStream.available();
            				if (bytesAvailable > 0) {
            					byte packetBytes [] =  new byte[bytesAvailable];
            					mInputStream.read(packetBytes);
            					
            					for (int i = 0; i < bytesAvailable; i++) {
            						byte b = packetBytes[i];
            						
            						if (b == delimiter) {
            							byte[] encodedBytes = new byte[readBufferPosition];
            							System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
            							
            							final String data = new String(encodedBytes, "US-ASCII");
            							readBufferPosition = 0;
            							
            							handler.post(new Runnable() {
            								public void run() {
            									Toast.makeText(getBaseContext(), data, Toast.LENGTH_LONG).show();
            								}
            							});
            						} else {
            							readBuffer[readBufferPosition++] = b;
            						}
            					}
            				}
            			} catch (IOException ioe) {
            				stopWorker = true;
            			}
            		}
            	}
            });
            workerThread.start();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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
