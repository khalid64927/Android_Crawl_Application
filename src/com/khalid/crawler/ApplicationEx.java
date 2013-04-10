package com.khalid.crawler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class ApplicationEx extends android.app.Application{
	private static String TAG = "ApplicationEx";
	public static final int CORE_POOL_SIZE = 30;
	private static final int MAXIMUM_POOL_SIZE = 50;
	
	public  static ThreadPoolExecutor operationsQueue;
	
	public  static ThreadPoolExecutor mediaOperationsQueue; 
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate--ApplicationExecutor--");
		operationsQueue = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 100000L, 
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
		
		mediaOperationsQueue = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 100000L, 
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
		
	}
	
	public static void reconstructThreadPool(){
		Log.d(TAG, "--reconstructThreadPool--");
		operationsQueue = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 100000L, 
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
	}
	public static void reconstructMediaThreadPool(){
		Log.d(TAG, "--reconstructThreadPool--");
		mediaOperationsQueue = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 100000L, 
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());
	}

}
