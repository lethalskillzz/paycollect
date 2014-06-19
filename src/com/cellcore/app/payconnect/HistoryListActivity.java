package com.cellcore.app.payconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cellcore.app.payconnect.ValidationActivity.PostDetails;

public class HistoryListActivity extends SherlockActivity {
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
    
    String values[][] = new String[5][6];
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historylistview);
		
		String json = getIntent().getStringExtra("values");
		
		System.out.println(json);
		ListView list = (ListView) findViewById(R.id.lisview1);
		
		try {
			JSONArray array = new JSONArray(json);
			
			
			int k = 0;
			for (int i = 0; i < 5; i++) {
				k=0;
				String string[] = new String[6];
				JSONObject object = array.getJSONObject(i);
				string[k++] = object.getString("amount");
				string[k++] = object.getString("agentId");
				string[k++] = object.getString("type");
				string[k++] = object.getString("account_number");
				string[k++] = object.getString("posttime");
				string[k++] = object.getString("transactionid");
				
				values[i] = string;
				
				//System.out.println(object.toString());
			}
			
			list.setAdapter(new ListViewAdapter(HistoryListActivity.this, values));
		} catch (JSONException je) {
			try {
				JSONObject object = new JSONObject(json);
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
				else
					message = "Error occurred";
				
				Utilities.showDialog(HistoryListActivity.this, "Error occurred", message).show();
			} catch (JSONException jee) {
				jee.printStackTrace();
				Utilities.showDialog(HistoryListActivity.this, "Error occurred", jee.getMessage()).show();
			}
		}
		
	}
	
	private class ListViewAdapter extends ArrayAdapter<String[]> {
		Context context;
		String array[][];
		
		int bg [] = new int[] {R.drawable.fancybg1, R.drawable.fancybg2};
		
		ListViewAdapter(Context context, String[][] data) {
			super(context, R.layout.row_listview,data);
			this.array = data;
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = convertView;
			
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.row_listview, null);
			}
			
			v.setBackgroundResource(bg[position%bg.length]);
			
			String [] arr = array[position];
			
			TextView trnxid = (TextView)v.findViewById(R.id.trnxid);
			TextView accnum = (TextView) v.findViewById(R.id.acctnum);
			TextView amount = (TextView) v.findViewById(R.id.amount);
			TextView type = (TextView) v.findViewById(R.id.type);
			TextView posttime = (TextView) v.findViewById(R.id.posttime);
			TextView agentid = (TextView) v.findViewById(R.id.agentid);
			
			trnxid.setText("Transaction ID: "+arr[5]);
			accnum.setText("Account number: "+arr[3]);
			amount.setText("Amount: "+arr[0]);
			type.setText("Transaction type: "+arr[2]);
			posttime.setText("Post time: "+arr[4]);
			agentid.setText("Agent ID: "+arr[1]);
			
			return v;
			
				
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.historylistmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.print:
				findBluetoothDevice();
				//Toast.makeText(getBaseContext(), "This is where i will put the code to print history", Toast.LENGTH_LONG).show();
		}
		return super.onOptionsItemSelected(item);
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
						if (device.getName().equals("MPT-II")) {
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
				openConnectionToBluetoothPrinter();
			}
			
			/**Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device  : pairedDevices) {
					if (device.getName().equals("MPT-II")) {
						mDevice = device;
						break;
					}
				}
			}
			
			Toast.makeText(getBaseContext(), "Bluetooth Device found", Toast.LENGTH_LONG).show();**/
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
			
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendDataTobePrinted() throws IOException {
		try {
			mOutputStream.write("--------------------------------\n".getBytes());
			
			
			new PrintImage().printTheImage(HistoryListActivity.this, mOutputStream, "paycol.bmp");
			for (int i = 0; i< 5; i++) {
				String msg  = "";
				String current[] = values[i];
				
				msg += "Transaction id: " + current[5] + "\n";
				msg += "Account number: " + current[3] + "\n";
				msg += "Amount: " + current[0] + "\n";
				msg += "Transaction type: " + current[2] + "\n";
				
				
				msg += "Post time: " + current[4] + "\n";
				
				msg += "--------------------------------\n";
				mOutputStream.write(msg.getBytes());
			}
			
			mOutputStream.write("\n\n".getBytes());
			
			closeConnection();
			Toast.makeText(getBaseContext(), "Data sent", Toast.LENGTH_LONG).show();
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
            sendDataTobePrinted();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
