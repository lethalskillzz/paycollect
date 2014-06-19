package com.cellcore.app.payconnect;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PrinterClass {
	String printtext;
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
    Context context;
	
	public PrinterClass(String text, Context context) {
		this.printtext = text;
		this.context = context;
	}
	
	private void findBluetoothDevice() {
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Toast.makeText(context, "No bluetooth adapter found", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				//context.startActivityForResult(enableBluetooth, 0);
				
			} else {
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device  : pairedDevices) {
						if (device.getName().equals("MPT-II")) {
							mDevice = device;
							break;
						}
					}
				}
				//openConnectionToBluetoothPrinter();
			}

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
