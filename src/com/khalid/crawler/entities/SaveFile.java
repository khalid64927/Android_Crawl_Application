package com.khalid.crawler.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;

public class SaveFile {
	
	private static final String TAG = "SaveFile";
	private InputStream mInputStream;
	private String mFileURL;
	private int mBufferSize;
	private boolean isSaved;
	
	public SaveFile(InputStream inputStream, String fileURL, int bufferSize) throws IOException{
		this.mInputStream = inputStream;
		this.mFileURL = fileURL;
		this.mBufferSize = bufferSize;
		saveFile(mInputStream,mFileURL,mBufferSize); 
	}

	private boolean saveFile(InputStream is, String filename, int bufferSize) throws IOException{
		boolean isError = false;
			String mediaFiles = filename.replace('/', '-');
			mediaFiles = mediaFiles.replace(':', '-');
			File directory = new File( 
					Environment.getExternalStorageDirectory(), "/Crawler"); 
			boolean exist = directory.exists();
			if (!exist) {
				directory.mkdirs();
			}else{
				// TODO:
			}
			File f = new File(directory, mediaFiles);
			Log.d(TAG, "file path " + f.getAbsolutePath());
			
			///writeURLtoFile(is, mediaFiles);
			writeURLtoFile(is, f, bufferSize);
		return isError;
	}
	
	/**
	 * Writes the contents of the url to a new file by calling saveURL with
	 * a file writer as argument
	 * @throws IOException 
	 */
	public static void writeURLtoFile(InputStream is, File file, int bufferSize) throws IOException{
		
		Log.d("writeURLtoFileAndroid","file :"+file+" buffer size :"+bufferSize);
		
		OutputStream out = null;
        out = new FileOutputStream(file);
        byte buf[]=new byte[bufferSize];
        int len;
        while((len = is.read(buf))>0){
        	out.write(buf,0,len);	 
        }

		

	}
}
