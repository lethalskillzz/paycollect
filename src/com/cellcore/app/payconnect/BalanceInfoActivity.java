package com.cellcore.app.payconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class BalanceInfoActivity extends SherlockActivity {
	TextView accountName, accountNumber, accountBal, phone, accountType, bankName;
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
    
    private ProgressDialog pDialog;
    
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balanceinfo);
		
		/**ActionBar bar = getSupportActionBar();
		bar.setHomeButtonEnabled(true);
		bar.setTitle("Balance Information");
		bar.show();**/
		
		Bundle bundle  = getIntent().getExtras();
		
		accountName = (TextView)findViewById(R.id.balaccountname);
		accountNumber = (TextView)findViewById(R.id.balaccountnumber);
		accountBal = (TextView)findViewById(R.id.balaccountbal);
		accountType = (TextView)findViewById(R.id.balaccounttype);
		phone  = (TextView)findViewById(R.id.balphone);
		bankName = (TextView)findViewById(R.id.balbankname);
		
		
		accountName.setText(bundle.getString("accountName"));
		accountNumber.setText(bundle.getString("accountNumber"));
		accountBal.setText(bundle.getString("bal"));
		accountType.setText(bundle.getString("type"));
		phone.setText(bundle.getString("phone"));
		bankName.setText(bundle.getString("bank"));
		
		TextView balTitle = (TextView)findViewById(R.id.baltitle);
		balTitle.setText("Balance info as at " + bundle.getString("lastUpdated"));
		
		Button button = (Button)findViewById(R.id.print);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				findBluetoothDevice();
			}
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
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
				}
			}
		} else Toast.makeText(getBaseContext(), "Bluetooth not turned on", Toast.LENGTH_LONG).show();
		
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

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendDataTobePrinted() throws IOException {
		try {
			mOutputStream.write("--------------------------------\n".getBytes());
			//new PrintImage().printTheImage(BalanceInfoActivity.this, mOutputStream, "paycol.bmp");
			String msg  = "";
			msg += "   Balance as at 2014-01-23\n\n";
			msg += "Account Name: " + accountName.getText().toString() + "\n";
			msg += "Account Number: " + accountNumber.getText().toString() + "\n";
			msg += "Account Balance: " + accountBal.getText().toString() + "\n";
			msg += "Account Type: " + accountType.getText().toString() + "\n";
			msg += "Phone Number: " + phone.getText().toString() +"\n";
			msg += "Bank Name: " + bankName.getText().toString() + "\n";
			msg += "\n--------------------------------\n";
		
			mOutputStream.write(msg.getBytes());
			
			finish();
			closeConnection();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void openConnectionToBluetoothPrinter() throws IOException {
		try {
			
			UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
			Method m = mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			mBluetoothSocket = (BluetoothSocket) m.invoke(mDevice, 1);
			//mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
			//mBluetoothAdapter.cancelDiscovery();
			mBluetoothSocket.connect();
			mOutputStream = mBluetoothSocket.getOutputStream();
			mInputStream = mBluetoothSocket.getInputStream();
			
			beginListenForData();
			
			Toast.makeText(getBaseContext(), "Bluetooth Connection opened", Toast.LENGTH_LONG).show();
			
			sendDataTobePrinted();
			
		} catch (NullPointerException e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("exception exception");
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
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
	
}
