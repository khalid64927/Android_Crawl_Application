package com.khalid.crawler.entities;

import java.util.NoSuchElementException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.khalid.crawler.ApplicationEx;
import com.khalid.crawler.interfaces.IPoolReportable;
import com.khalid.crawler.queue.URLPool;
import com.khalid.crawler.threads.GetMediaService;

public class MediaLinkProcessor implements IPoolReportable{
	private static final String TAG = "ProcessMediaLinks";
	private Context mContext;
	private static int mImageCount;
	
	public MediaLinkProcessor(Context context) {
		URLPool.getInstance().setMediaPoolListener(this);
	}

	@Override
	public void onURLPoolEntryAdded(String url) {
		if(!ApplicationEx.operationsQueue.isShutdown()){
			loadBalanc();
		}
		Log.e(TAG, "onURLPoolEntryAdded ");
	}

	@Override
	public void onURLMediaLinkSaved(String savedLink) {
		Log.e(TAG, ++mImageCount+"onURLMediaLinkSaved  "+savedLink);
		
	}

	@Override
	public void onURLMediaLinkError(String errorLink) {
		Log.e(TAG, "onURLMediaLinkError" +errorLink);
		URLPool.getInstance().pushFailedUri(errorLink);
	}
	
	private synchronized void loadBalanc(){
			
			Log.e("loadBalanc", "active counts"+ApplicationEx.mediaOperationsQueue.getActiveCount()+" core pool size "+ApplicationEx.mediaOperationsQueue.getCorePoolSize());
			
			if(ApplicationEx.mediaOperationsQueue.getActiveCount() == ApplicationEx.CORE_POOL_SIZE){
				// not adding to queue
				Log.e("returning ---", "--");
				return;		
			}else{
				if(ApplicationEx.mediaOperationsQueue.isShutdown()){
					Log.e(TAG, "queue reconstructThreadPool  ----");
					ApplicationEx.reconstructMediaThreadPool();
				}
				startDownloadingFiles();
			}		
		
	}
	
	private void startDownloadingFiles(){
		String fileURL = ""; 
		try{
		if(!(URLPool.getInstance().getQueuedMediaPoolSize() > 0)){
			Toast.makeText(mContext, "No Media to download", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.v(TAG, "startDownloadingFiles");
		fileURL = URLPool.getInstance().popMediaPool();
		GetMediaService service = new GetMediaService(fileURL);
		service.setListener(this);
		ApplicationEx.mediaOperationsQueue.execute(service);
		}catch (NoSuchElementException nsee) {
			nsee.printStackTrace();
			Log.e("startDownloadingFiles", nsee.getMessage());
		}
	}


}
