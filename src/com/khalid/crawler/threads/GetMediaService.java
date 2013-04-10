package com.khalid.crawler.threads;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import android.text.TextUtils;
import android.util.Log;

import com.khalid.crawler.entities.SaveFile;
import com.khalid.crawler.interfaces.IPoolReportable;

public class GetMediaService implements Runnable
{
	public GetMediaService(String url) {
		this.imageURL = url;
	}
	public GetMediaService(){
	}
	
	private IPoolReportable listener;
	private String imageURL;
	private String errorMessage;
	private static final String TAG = "GetMediaService";
	private SaveFile mSaveFile;

	private InputStream mMediaStream;
	
	public void run()
	{		
		Log.d(TAG, "run : "+imageURL);
		try
		{
			com.khalid.crawler.HTTPRequest request = new com.khalid.crawler.HTTPRequest(imageURL);
			request.execute(com.khalid.crawler.HTTPRequest.RequestMethod.GET);
			Log.d(TAG, "execute");
			mMediaStream = request.getMediaStream();
			
			
			if(TextUtils.isEmpty(mMediaStream.toString())){
				errorMessage = "No Media data";
				listener.onURLMediaLinkError(imageURL);
			}else{
				mSaveFile = new SaveFile(mMediaStream,imageURL, request.getmBuffferSize());
				listener.onURLMediaLinkSaved(imageURL);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Log.e(TAG, "------------run catch exception----------");
			errorMessage = "No Media data";
			listener.onURLMediaLinkError(imageURL);
		}
	}

	
	/**
	 * Writes the contents of the url to a new file by calling saveURL with
	 * a file writer as argument
	 */
	public static void writeURLtoFile(InputStream is, String filename)
		throws IOException {
		FileWriter writer = new FileWriter(filename);
		saveURL(is, writer);
		writer.close();
		FileOutputStream os = new FileOutputStream(filename);
		saveURL(is, os);
		os.close();
	}
	
	/**
	 * Opens a buffered stream on the url and copies the contents to writer
	 */
	public static void saveURL(InputStream is, Writer writer)
		throws IOException {
		BufferedInputStream in = new BufferedInputStream(is);
		for (int c = in.read(); c != -1; c = in.read()) {
			writer.write(c);
		}
	}
		
	/**
	 * Opens a buffered stream on the url and copies the contents to OutputStream
	 */
	public static void saveURL(InputStream is, OutputStream os)
		throws IOException {
		byte[] buf = new byte[1048576];
		int n = is.read(buf);
		while (n != -1) {
			os.write(buf, 0, n);
			n = is.read(buf);
		}
	}

	public void setListener(IPoolReportable listener)
	{
		this.listener = listener;
	}

	public IPoolReportable getListner()
	{ 
		 return this.listener;
	}

	
	
}

