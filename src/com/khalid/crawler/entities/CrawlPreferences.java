package com.khalid.crawler.entities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class CrawlPreferences {
	
	public CrawlPreferences(String seedURL, String searchString, boolean isDomainSpecific, int deapthLevel, ArrayList<String> fileTypes,float filesize) {
	this.mSeedURL = seedURL;
	this.mSearchString = searchString;
	this.mIsDomainSpecific = isDomainSpecific;
	this.mDeapthLevel = deapthLevel;
	this.mMediaTypes = fileTypes;
	validateInputs(filesize);
	setHost(seedURL);
	}
	
	private String mSeedURL;
	private String mSearchString;
	private boolean mIsDomainSpecific;
	private int mDeapthLevel;
	private ArrayList<String> mMediaTypes;
	private double mFileSize;
	private String mHost;
		
	public String getmSeedURL() {
		return mSeedURL;
	}
	public void setmSeedURL(String mSeedURL) {
		this.mSeedURL = mSeedURL;
	}
	public String getmSearchString() {
		return mSearchString;
	}
	public void setmSearchString(String mSearchString) {
		this.mSearchString = mSearchString;
	}
	public boolean ismIsDomainSpecific() {
		return mIsDomainSpecific;
	}
	public void setmIsDomainSpecific(boolean mIsDomainSpecific) {
		this.mIsDomainSpecific = mIsDomainSpecific;
	}
	public int getmDeapthLevel() {
		return mDeapthLevel;
	}
	public void setmDeapthLevel(int mDeapthLevel) {
		this.mDeapthLevel = mDeapthLevel;
	}
	public ArrayList<String> getmMediaTypes() {
		return mMediaTypes;
	}
	public void setmMediaTypes(ArrayList<String> mMediaTypes) {
		this.mMediaTypes = mMediaTypes;
	}
	public double getmFileSize() {
		return mFileSize;
	}
	public void setmFileSize(double mFileSize) {
		this.mFileSize = mFileSize;
	}
	
	public String getmHost() {
		return mHost;
	}
	public void setmHost(String mHost) {
		this.mHost = mHost;
	}
	
	private void setHost(String urlString){
		try {
			URL url = new URL(urlString);
			setmHost(url.getHost());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	private void validateInputs(Float fileSize){
		if(fileSize != null){
			this.mFileSize = fileSize * 1048576;
		}else{
			mFileSize = -1;
		}
		
	}
	
	

}
