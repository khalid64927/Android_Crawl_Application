package com.khalid.crawler.queue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.khalid.crawler.entities.CrawlPreferences;
import com.khalid.crawler.interfaces.IPoolReportable;

public class URLPool {

	private static URLPool mURLPool = null;

	public static URLPool getInstance() {
		if (mURLPool == null) {
			mURLPool = new URLPool();
			return mURLPool;
		}
		return mURLPool;
	}

	private IPoolReportable listener;

	public Set<String> _URLPool = new HashSet<String>();
	public LinkedList<String> urlPoolList = new LinkedList<String>();
	public LinkedList<String> urlProcessedPoolList = new LinkedList<String>();

	public Set<String> urlMediaPoolSet = new HashSet<String>();
	public LinkedList<String> urlMediaPoolList = new LinkedList<String>();
	public LinkedList<String> urlMediaSavedPool = new LinkedList<String>();
	private AtomicBoolean isPoolSizeReached = new AtomicBoolean();
	private CrawlPreferences mCrawlPreferences;
	
	public CrawlPreferences getmCrawlPreferences() {
		return mCrawlPreferences;
	}
	
	public void setmCrawlPreferences(CrawlPreferences mCrawlPreferences) {
		this.mCrawlPreferences = mCrawlPreferences;
	}

	public Set<String> getUrlPoolSet() {
		return _URLPool;
	}

	public LinkedList<String> getUrlPoolList() {
		return urlPoolList;
	}

	public LinkedList<String> getUrlProcessedPoolList() {
		return urlProcessedPoolList;
	}

	public LinkedList<String> getUrlMediaPoolSet() {
		return urlMediaPoolList;
	}

	public LinkedList<String> getUrlMediaPoolList() {
		return urlMediaPoolList;
	}

	public LinkedList<String> getUrlMediaSavedPool() {
		return urlMediaSavedPool;
	}

	public boolean hasPoolSizeReached() {
		return isPoolSizeReached.get();
	}

	public synchronized int getUrlPoolSetSize() {
		return _URLPool.size();
	}

	/**
	 * Clear all of the workloads.
	 */
	public void clear() {
		getUrlPoolList().clear();
		getUrlPoolSet().clear();
		getUrlProcessedPoolSet().clear();
		getUrlMediaSavedPool().clear();
	}

	public synchronized boolean push(String url) {
		boolean isSucess = false;
		if (_URLPool.add(url)) {
			// save if it is a media link
			if (isMediaLink(url)) {
				if (urlMediaPoolSet.add(url)) {
					urlMediaPoolList.addLast(url);
					listener.onURLPoolEntryAdded(url);
				}
			} else {
				urlPoolList.addLast(url);
			}
			isSucess = true;
		}
		return isSucess;
	}

	/*public synchronized boolean pop(String link) {
		boolean isSuccess = false;
		if (urlPoolList.size() != 0) {
			if(urlPoolList.remove(link)){
			urlProcessedPoolList.add(link);
			isSuccess = true;
			}
		}
		return isSuccess;
	}*/
	
	public synchronized String pop() {
		String url = "";
		if (urlPoolList.size() != 0) {
			url = urlPoolList.removeFirst();
		}
		return url;
	}

	private boolean isMediaLink(String url) {
		boolean islink = false;
		try {
			URL pageURL = new URL(url);

			// See if it's a jpeg, jpg and size is more than 4mb
			URLConnection conn = pageURL.openConnection();
			String filename = pageURL.getFile().toLowerCase();
			if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")
					|| filename.endsWith(".png")) {
				// if(conn.getContentLength()>= 4096)
				islink = true;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			islink = false;
		}catch (IOException e) {
			e.printStackTrace();
			islink = false;
		}
		return islink;
	}
	
	
	/*public synchronized String getLink(){
		String url = null;
		if (urlPoolList.size() != 0) {
			url = urlPoolList.getFirst();
		}
		return url;
	}*/

	public IPoolReportable getListener() {
		return listener;
	}

	public void setMediaPoolListener(IPoolReportable lisetener) {
		this.listener = lisetener;
	}

	public synchronized int getUrlPoolSize() {
		return _URLPool.size();
	}

	public synchronized int getUrlProcessedPoolSize() {
		return urlProcessedPoolList.size();
	}

	public synchronized int getMediaPoolSize() {
		return urlMediaPoolSet.size();
	}

	public synchronized int getQueuedMediaPoolSize() {
		return urlMediaPoolList.size();
	}

	public synchronized int getSavedMediaPoolSize() {
		return urlMediaSavedPool.size();
	}

	public LinkedList<String> getUrlProcessedPoolSet() {
		return urlProcessedPoolList;
	}

	public synchronized boolean pushMediaURL(String imageURL) {
		boolean isSuccess = false;
		if (urlMediaPoolSet.add(imageURL)) {
			urlMediaPoolList.addLast(imageURL);
			listener.onURLPoolEntryAdded(imageURL);
			isSuccess = true;
		}
		return isSuccess;
	}

	/*public synchronized boolean popMediaPool(String mediaLink) throws NoSuchElementException {
			boolean isSuccess = false;
			if (urlMediaPoolList.size() != 0) {
				if(urlMediaPoolList.remove(mediaLink)){
					isSuccess = true;
				}
			}
			return isSuccess;
	}*/
	public synchronized String popMediaPool() throws NoSuchElementException {
		String url = "";
		if (urlMediaPoolList.size() != 0) {
			url = urlMediaPoolList.removeFirst();
		}
		return url;
}

	
	/*public synchronized String getMediaLink()  throws NoSuchElementException{
			String url = null;
			if (urlMediaPoolList.size() != 0) {
				url = urlMediaPoolList.getFirst();
			}
			return url;
	}*/
	
	
	public synchronized boolean pushFailedUri(String uri){
		boolean isSuccess = true;
		try{
			urlMediaPoolList.addLast(uri);
		}catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
		}
		return isSuccess;
	}

}
