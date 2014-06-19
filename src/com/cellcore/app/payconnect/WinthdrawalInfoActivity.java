package com.cellcore.app.payconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class WinthdrawalInfoActivity extends SherlockActivity {
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
    
    TextView transid, transdate, transamt, withtype;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.withdrawalinfo);
		
		Bundle extras = getIntent().getExtras();
		
		transid = (TextView)findViewById(R.id.transid);
		transdate = (TextView)findViewById(R.id.transdate);
		transamt = (TextView)findViewById(R.id.transamt);
		withtype = (TextView)findViewById(R.id.withdawaltype);
		
		transid.setText("TransactionID: " + extras.getString("id"));
		transdate.setText("Date: " + extras.getString("date"));
		transamt.setText("Amount: " + extras.getString("amount"));
		withtype.setText("Transaction type: withdrawal");
		
		Button print = (Button)findViewById(R.id.print);
		print.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				findBluetoothDevice();
				//Toast.makeText(getBaseContext(), "printing...", Toast.LENGTH_SHORT).show();
			}
		});
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
			
			if (sendDataTobePrinted())
				if (sendDataTobePrinted())
					closeConnection();
				else
					closeConnection();
			
		} catch (NullPointerException e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("exception exception");
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	private boolean sendDataTobePrinted() throws IOException {
		try {
			mOutputStream.write("--------------------------------\n".getBytes());
			new PrintImage().printTheImage(WinthdrawalInfoActivity.this, mOutputStream, "paycol.bmp");
			String msg  = "";
			
			//String msg  = "--------------------------------\n";
			msg += transid.getText().toString() + "\n";
			msg += transamt.getText().toString() + "\n";
			msg += transdate.getText().toString() + "\n";
			msg += withtype.getText().toString() + "\n";
			msg += "\n--------------------------------\n";
			msg += "\n\n";
		
			mOutputStream.write(msg.getBytes());
			
			finish();
			return true;
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
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
