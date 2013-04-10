package com.khalid.crawler.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myandroidspider.R;
import com.khalid.crawler.ApplicationEx;
import com.khalid.crawler.entities.CrawlPreferences;
import com.khalid.crawler.entities.MediaLinkProcessor;
import com.khalid.crawler.interfaces.ICrawlerReportable;
import com.khalid.crawler.queue.URLPool;
import com.khalid.crawler.threads.GetLinksService;
import com.khalid.crawler.util.Utility;

public class MainActivity extends Activity implements ICrawlerReportable {
	private static final String TAG = "MainActivity";

	private EditText mUrl;
	private EditText mSearchString;
	private EditText mDeapth;
	private EditText mFileSize;
	private Button mFileTypeButton;
	private RadioButton mIsDomain;
	private Button mButton1;
	private TextView mCurrentLinkText;
	private TextView mLinksCountText;
	private TextView mProcessedLinksCountText;
	private TextView mImageCountTextView;
	private Context mContext;
	private Utility mUtil;

	private String mPageURL;
	private boolean mIsActive = false;
	private ProgressBar mProgressBar;
	private MediaLinkProcessor mMediaProcessor;
	private CrawlPreferences mCrawlPref;
	private String[] mFileType;
	private ArrayAdapter<String> mAdapter;
	private ArrayList<String> selectedFileTypes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initializing UI elements
		mUrl = (EditText) findViewById(R.id.urlEditText);
		mButton1 = (Button) findViewById(R.id.beginButton);
		mCurrentLinkText = (TextView) findViewById(R.id.currentLinkTextView);
		mLinksCountText = (TextView) findViewById(R.id.goodLinkTextView);
		mImageCountTextView = (TextView)findViewById(R.id.imageCountTextView);
		mProcessedLinksCountText = (TextView) findViewById(R.id.badLinkTextView);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

		mFileTypeButton = (Button) findViewById(R.id.filetypes);
		mFileSize = (EditText) findViewById(R.id.filesize);
		mDeapth = (EditText) findViewById(R.id.depth);
		mSearchString = (EditText) findViewById(R.id.searchString);
		mIsDomain = (RadioButton) findViewById(R.id.domain);

		mMediaProcessor = new MediaLinkProcessor(this);
		mUtil = new Utility(this);
		hideSoftKeyboard(mUrl);
		hideSoftKeyboard(mFileSize);
		hideSoftKeyboard(mDeapth);

	}

	public void beginButtonClicked(View view) {

		if (!((mUtil.validatURL(mUrl.getText().toString(), this) && (mUtil
				.isIntenetAvailable(this))))) {
			return;
		}
		mPageURL = mUrl.getText().toString();
		if (mIsActive) {
			stopCrawling();
		} else {
			mIsActive = true;
			mButton1.setText(R.string.cancle_text);
			Toast.makeText(this, "Starting Crawler", Toast.LENGTH_SHORT).show();
			URLPool.getInstance().push(mPageURL);
			progressStart();
			startCrawling(getData());
		}
	}

	/**
	 * This method hides the soft keyboard
	 */
	private void hideSoftKeyboard(EditText editTexttoHide) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editTexttoHide.getWindowToken(), 0);
	}

	private CrawlPreferences getData() {
		int deapth;
		float fileSize;
		// get deapth
		if (TextUtils.isEmpty(mDeapth.getText().toString())) {
			deapth = -1;
		} else {
			deapth = Integer.parseInt(mDeapth.getText().toString());
			System.out.println(" DEAPTH -------------->" + deapth);
		}
		// file size
		if (TextUtils.isEmpty(mFileSize.getText().toString())) {
			fileSize = -1;
		} else {
			fileSize = Float.parseFloat(mFileSize.getText().toString());
			System.out.println(" FILESIZE -------------->" + fileSize);
		}
		
		System.out.println(" SEARCH STRING -------------->"
				+ mSearchString.getText().toString());
		mCrawlPref = new CrawlPreferences(mPageURL, mSearchString.getText()
				.toString(), mIsDomain.isChecked(), deapth, selectedFileTypes,
				fileSize);
		URLPool.getInstance().setmCrawlPreferences(mCrawlPref);
		return mCrawlPref;
	}

	public void onSelectFilesClicked(View view) {
		selectedFileTypes = new ArrayList<String>();
		mFileType = getResources().getStringArray(R.array.file_type);
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_multichoice);
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setMultiChoiceItems(R.array.file_type, null,
				new DialogInterface.OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						if (isChecked) {
							selectedFileTypes.add(mFileType[which]);
						} else {
							selectedFileTypes.remove(mFileType[which]);
						}

					}
				});
	}

	private void startCrawling(CrawlPreferences crawlPref) {
		Log.v(TAG, "startCrawling");
		try {
			/**
			 * Before reconstructing ThreadPoolExecutor do some validation 
			 * 1> Was shutdown called with proper flags 
			 *     <a> start crawling flag (mIsActive) should be set 
			 *         and pool size should be less than its limit
			 */
			if ((ApplicationEx.operationsQueue.isShutdown()) && (mIsActive)) {
				// calling start again
				ApplicationEx.reconstructThreadPool();
			}
			/**
			 * check if pool has URLs to be crawled
			 */
			getLink();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopCrawling() {
		// avoid execution when crawling has already been interrupted by crawler
		// itself due to Queue limit reached or empty Queue
		if (!ApplicationEx.operationsQueue.isShutdown()) {
			mIsActive = false;
			mButton1.setText(R.string.begin_text);
			Toast.makeText(this, "Crawler Shutting down", Toast.LENGTH_SHORT)
					.show();
			ApplicationEx.operationsQueue.shutdownNow();
			ApplicationEx.operationsQueue.getQueue().clear();

			// current link count
			mLinksCountText.setText(getString(R.string.links_count) + " "
					+ URLPool.getInstance().getUrlPoolSize());

			// current processed pool count
			mProcessedLinksCountText
					.setText(getString(R.string.processed_links_count) + " "
							+ URLPool.getInstance().getUrlProcessedPoolSize());
			Log.e("link count", " count "
					+ URLPool.getInstance().getUrlPoolSize());
			Log.e("processed link count", " count "
					+ URLPool.getInstance().getUrlProcessedPoolSize());
			progressStop();
		}
	}

	private void loadBalancCrawler() {
		Log.v(TAG, "loadBalancCrawler "
				+ ApplicationEx.operationsQueue.getActiveCount() );
		// check if all core threads are running
		if ((ApplicationEx.operationsQueue.getActiveCount() == ApplicationEx.CORE_POOL_SIZE)) {
			// if all core threads are running then check url pool
			if (!TextUtils.isEmpty(getLink())) {
				Log.e("returning", "loadBalancCrawler");
				return;
			}
		}
		startCrawling(mCrawlPref);
	}

	private String getLink() {
		Log.v(TAG, "getLink");
		String pageURL = URLPool.getInstance().pop();
		if (!TextUtils.isEmpty(pageURL)) {
			Log.v("startCrawling", pageURL);
			GetLinksService service = new GetLinksService(pageURL, this,
					mCrawlPref);
			ApplicationEx.operationsQueue.execute(service);
		} else {
			Log.v(TAG, "getLink"+pageURL);
			// either pool is empty or pool limit has reached
			Toast.makeText(this,
					R.string.empty_queue,
					Toast.LENGTH_SHORT).show();
			stopCrawling();
		}
		return pageURL;
	}

	/**
	 * start progress bar
	 * 
	 */
	private void progressStart() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * stop progress bar
	 * 
	 */
	private void progressStop() {
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void spiderFoundURL(String urlString) {
		Log.i("spiderFoundURL", "urlString");
		Message message = Message.obtain();
		message.what = 1;
		message.obj = (String) urlString;
		crawlerHandler.sendMessage(message);
	}

	@Override
	public void spiderURLProcessed(int processedURLCount) {
		// Log.v(TAG, "spiderURLProcessed " + processedURLCount);
		Message message = Message.obtain();
		message.what = 3;
		message.obj = (int) processedURLCount;
		crawlerHandler.sendMessage(message);

	}

	@Override
	public void spiderLinkCounts(int goodLinkCount) {
		// Log.v(TAG, "spiderGoodLinkCounts");
		Message message = Message.obtain();
		message.what = 2;
		message.obj = (int) goodLinkCount;
		crawlerHandler.sendMessage(message);

	}

	@Override
	public void finished(String url) {
		Log.v(TAG, "finished");
		Message message = Message.obtain();
		message.what = 5;
		message.obj = (String) url;
		crawlerHandler.sendMessage(message);
	}

	public android.os.Handler crawlerHandler = new android.os.Handler() {
		public void handleMessage(Message msg) {

			// current URL
			if (msg.what == 1) {
				mCurrentLinkText.setText(getString(R.string.current_link) + " "
						+ (String) msg.obj);

			}
			// current link count
			if (msg.what == 2)
				mLinksCountText.setText(getString(R.string.links_count) + " "
						+ msg.obj);
			
			// processed link count
			if (msg.what == 3) {
				mProcessedLinksCountText
						.setText(getString(R.string.processed_links_count)
								+ " " + msg.obj);
			}

			// pool size reached
			if (msg.what == 4) {
				stopCrawling();
			}
			if (msg.what == 5) {
				URLPool.getInstance().urlProcessedPoolList.add((String)msg.obj);
				loadBalancCrawler();
				/*
				 * // don't call if shutdown has been called, if stopCrawling
				 * flag is active
				 * if((!ApplicationEx.operationsQueue.isShutdown()
				 * )&&(mIsActive)){ startCrawling(mCrawlPref); }
				 */
			}

		};
	};

}
