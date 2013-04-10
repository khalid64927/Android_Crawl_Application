package com.khalid.crawler.threads;

import android.text.TextUtils;
import android.util.Log;

import com.khalid.crawler.entities.CrawlLinks;
import com.khalid.crawler.entities.CrawlPreferences;
import com.khalid.crawler.interfaces.ICrawlerReportable;
import com.khalid.crawler.queue.URLPool;

public class GetLinksService implements Runnable {
	private static final String TAG = "GetLinksService";
	private String mURL;
	private ICrawlerReportable report;
	private com.khalid.crawler.HTTPRequest mRequest;
	private CrawlLinks mCrawlLinks;

	/**
	 * A flag that indicates whether this process should be canceled
	 */
	protected boolean cancel = false;

	public GetLinksService(String url, ICrawlerReportable report,
			CrawlPreferences crawlPref) {
		this.mURL = url;
		this.report = report;
		mRequest = new com.khalid.crawler.HTTPRequest(mURL);
		mCrawlLinks = new CrawlLinks(report);
	}

	public void run() {
		Log.v(TAG, "run");
			if(!processURL()){
				URLPool.getInstance().pushFailedUri(mURL);
			}else{
				
			}
		report.finished(mURL);
	}

	/**
	 * Called internally to process a URL
	 */
	public boolean processURL() {
		boolean isSucess = false;
		try {
			String response = invokeService(mURL);
			if (!TextUtils.isEmpty(response)) {
				isSucess = true;
				Log.v(TAG, "processURL");
				mCrawlLinks.extractLinks(response, mURL);

			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("processURL", "error");
		}
		
		return isSucess;
	}

	/**
	 * function which gets web content
	 * 
	 * @param1 String: URL
	 */
	private String invokeService(String url) {
		String response = null;
		try {
			mRequest.addHeader("Accept-Encoding", "gzip");
			mRequest.execute(com.khalid.crawler.HTTPRequest.RequestMethod.GET);
			response = mRequest.getResponseString();
		//	Log.d("invokeservice", response);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

}