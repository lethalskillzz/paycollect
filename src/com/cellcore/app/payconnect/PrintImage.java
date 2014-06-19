package com.cellcore.app.payconnect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.BitSet;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class PrintImage {
	int mWidth;
	int mHeight;
	BitSet dots;
	
	public static void writeImageToSDCard(Context context) {
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.paycollectlogotest);
		
		File sd = Environment.getExternalStorageDirectory();
	    String fileName = "testimage.png";
	    File dest = new File(sd, fileName);
	    try {
	        FileOutputStream out;
	        out = new FileOutputStream(dest);
	        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
	        out.flush();
	        out.close();
	    } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	public byte[] hexToBuffer(String hexString) throws NumberFormatException{
		int length = hexString.length();
	    byte[] buffer = new byte[(length + 1) / 2];
	    boolean evenByte = true;
	    byte nextByte = 0;
	    int bufferOffset = 0;

	    if ((length % 2) == 1) {
	        evenByte = false;
	    }
	    
	    for (int i = 0; i < length; i++) {
	        char c = hexString.charAt(i);
	        int nibble; // A "nibble" is 4 bits: a decimal 0..15

	        if ((c >= '0') && (c <= '9')) {
	            nibble = c - '0';
	        } else if ((c >= 'A') && (c <= 'F')) {
	            nibble = c - 'A' + (byte)0x0A;
	        } else if ((c >= 'a') && (c <= 'f')) {
	            nibble = c - 'a' + (byte)0x0A;
	        } else {
	            throw new NumberFormatException("Invalid hex digit '" + c +
	                "'.");
	        }

	        if (evenByte) {
	            nextByte = (byte) (nibble << 4);
	        } else {
	            nextByte += (byte) nibble;
	            buffer[bufferOffset++] = nextByte;
	        }

	        evenByte = !evenByte;
	    }

	    return buffer;
	}
	
	public void printTheImage(Context context, OutputStream outputStream, String fileName) {
		
		File sdcard = Environment.getExternalStorageDirectory();
		File f1 = new File(sdcard, fileName);
		
		//File f1 = getFileFromRawResource(context, uri);
		
		
		try {
			if (f1.exists()) {
				//Toast.makeText(context, "file exists", Toast.LENGTH_LONG).show();
				
				Bitmap bmp = BitmapFactory.decodeFile(f1.getAbsolutePath());
				convertBitmap(bmp, context);
			
				outputStream.write(PrinterCommands.SET_LINE_SPACING_24);
				
				int offset = 0;
				
				while (offset < bmp.getHeight()) {
					//Toast.makeText(context, "in while: offset is: " + offset, Toast.LENGTH_SHORT).show();
					
					outputStream.write(PrinterCommands.SELECT_BIT_IMAGE_MODE);
					for (int x = 0; x < bmp.getWidth(); ++x) { 
						for (int k = 0; k <3; ++k) {
							byte slice = 0;
							for (int b = 0; b < 8; ++b) {
								int y = (((offset/8) + k) * 8) + b;
								int i = (y * bmp.getWidth()) + x;
								boolean v = false;
								if (i < dots.length()) {
									v = dots.get(i);
								}
								
								slice |= (byte) ((v ? 1 : 0)<< (7-b));
							}
							outputStream.write(slice);
						}
					}
					
					offset += 24;
					outputStream.write(PrinterCommands.FEED_LINE);
					/**outputStream.write(PrinterCommands.FEED_LINE);
					outputStream.write(PrinterCommands.FEED_LINE);
					outputStream.write(PrinterCommands.FEED_LINE);
					outputStream.write(PrinterCommands.FEED_LINE);
					outputStream.write(PrinterCommands.FEED_LINE);**/
				}
				
				outputStream.write(PrinterCommands.SET_LINE_SPACING_30);
				System.out.println("got to th end of printing");
				
			} else {
				Toast.makeText(context, "file does not exist", Toast.LENGTH_SHORT).show();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public  String convertBitmap(Bitmap inputBitmap, Context context) {
        mWidth = inputBitmap.getWidth();
        mHeight = inputBitmap.getHeight();
        convertArrayToGrayScale(inputBitmap, mWidth, mHeight, context);
        
        return "ok";
    }
	
	private void convertArrayToGrayScale(Bitmap original, int width, int height, Context context) {
		int pixel;
		int k = 0;
		int B=0, G=0, R=0;
		dots = new BitSet();
		
		
		try {
			for (int x = 0; x < height; x++) {
				for (int y = 0; y < width; y++) {
					pixel = original.getPixel(y, x);
				
					//retrieve color of all channels
					R = Color.red(pixel);
					G = Color.green(pixel);
					B = Color.blue(pixel);
				
					//take conversion up to one single value by calculating pixel intensity
					R=G=B=(int) (0.299 * R + 0.587 * G + 0.114 * B);
				
					//set bit into bitset, by calculating the pixel's luma
					if (R < 55) {
						dots.set(k);//this is the bitset that i'm printing
					}
					k++;
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
