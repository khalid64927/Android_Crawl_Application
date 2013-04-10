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

public class BasePoolClass implements IPoolCrawl {

	public Set<String> _URLPool = new HashSet<String>();
	public LinkedList<String> urlPoolList = new LinkedList<String>();
	public LinkedList<String> urlProcessedPoolList = new LinkedList<String>();

	public Set<String> urlMediaPoolSet = new HashSet<String>();
	public LinkedList<String> urlMediaPoolList = new LinkedList<String>();
	public LinkedList<String> urlMediaSavedPool = new LinkedList<String>();
	private AtomicBoolean isPoolSizeReached = new AtomicBoolean();

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
		} catch (IOException e) {
			e.printStackTrace();
			islink = false;
		}
		return islink;
	}

	@Override
	public synchronized boolean push(String url) {
		boolean isSucess = false;
		if (_URLPool.add(url)) {
			// save if it is a media link
			if (isMediaLink(url)) {
				if (urlMediaPoolSet.add(url)) {
					urlMediaPoolList.addLast(url);
					// listener.onURLPoolEntryAdded(url);
				}
			} else {
				urlPoolList.addLast(url);
			}
			isSucess = true;
		}
		return isSucess;
	}

	@Override
	public synchronized String pop() {
		String url = "";
		if (urlPoolList.size() != 0) {
			url = urlPoolList.removeFirst();
		}
		return url;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}

class ImagePool extends BasePoolClass {
	public synchronized boolean popMediaPool(String mediaLink)
			throws NoSuchElementException {
		boolean isSuccess = false;
		if (urlMediaPoolList.size() != 0) {
			if (urlMediaPoolList.remove(mediaLink)) {
				isSuccess = true;
			}
		}
		return isSuccess;
	}

	public synchronized boolean pushMediaURL(String imageURL) {
		boolean isSuccess = false;
		if (urlMediaPoolSet.add(imageURL)) {
			urlMediaPoolList.addLast(imageURL);
			isSuccess = true;
		}
		return isSuccess;
	}
}

class AudioPool extends BasePoolClass{
	
}