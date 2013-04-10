package com.khalid.crawler.entities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import android.text.TextUtils;
import android.util.Log;

import com.khalid.crawler.ApplicationEx;
import com.khalid.crawler.HTTPRequest;
import com.khalid.crawler.interfaces.ICrawlerReportable;
import com.khalid.crawler.queue.URLPool;

public class CrawlLinks {
	private ICrawlerReportable report;
	private static final String TAG = "CrawlLinks";
	private CrawlPreferences crawlPreferences;
	private com.khalid.crawler.HTTPRequest mRequest;

	public CrawlLinks() {
	}

	public CrawlLinks(ICrawlerReportable report) {
		this.report = report;
		crawlPreferences = URLPool.getInstance().getmCrawlPreferences();
	}
	
	/**
	 * Single site crawl validation
	 *  1> First check Whether single domain needs to crawled
	 *  2> Check for host name new link is within the same domain
	*/
	private boolean isFromSameDomain(String url){
		boolean isSameHost = true;
		if((!TextUtils.isEmpty(crawlPreferences.getmHost())) && (!TextUtils.isEmpty(getHost(url)))){
			if(!TextUtils.equals(crawlPreferences.getmHost(),getHost(url))){
				System.out.println(" not in domain returning ");
				isSameHost = false;
			}
		}
		return isSameHost;
	}
	private String getHost(String urlString){
		String host = "";
		try {
			URL url = new URL(urlString);
			host = url.getHost();
			Log.d("host", " -- "+host);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return host;
	}

	/**
	 * function which extracts links form web page
	 * @param2 String: Web page
	 * @param3 String: URL of the page to be crawled
	 */
	public void extractLinks(String page, String url) {
		Log.v(TAG, "extractLinks "+url);
		
		// commenting domain specific search
		/*if(!searchTypeCheck(url)){
			return;
		}*/
		
		String rawPage = page;
		Log.v("after host validation", url);
		int index = 0;
		extractImageLinks2(page, url);
		while ((index = page.indexOf("<a ", index)) != -1) {
			int tagEnd = page.indexOf(">", index);
			if ((index = page.indexOf("href", index)) == -1)
				break;
			if ((index = page.indexOf("=", index)) == -1)
				break;
			int endTag = page.indexOf("</a", index);
			String remaining = rawPage.substring(++index);
			StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\"'>#");
			String strLink = st.nextToken();

			if ((TextUtils.equals(strLink, "javascript:void(0)"))
					|| (TextUtils.equals(strLink, "javascript:void(0);"))) {
				// avoiding javscript calls
				continue;
			}
			
			if (!checkLinks(strLink)) {
				strLink = makeAbsoluteURL(url, strLink);
			}
			
			if (!TextUtils.isEmpty(strLink)) {
				// validate if it is a media link and if string search is enabled proceed for further search preference filtration
				if(isMediaLink(strLink) && (!TextUtils.isEmpty(crawlPreferences.getmSearchString()))){
					if(verifyTitleTag(index, rawPage, tagEnd, endTag)){
						if(crawlPreferences.getmFileSize() != -1){
							mRequest = new HTTPRequest(strLink);
							float mediaSize = mRequest.getMediaContentLength(strLink, false);
							if((mediaSize == -1) || (mediaSize < crawlPreferences.getmFileSize())){
								// size not matching as per the preferences move forward to next link
								continue;
							}else{
							//	System.out.println(" adding from isMediaLink");
								addLink(strLink);
							}
						}
					}
				}else{
					System.out.println(" adding non media link "+strLink);
					addLink(strLink);
				}
				
			}
		}
		report.spiderURLProcessed(URLPool.getInstance()
				.getUrlProcessedPoolSize());
	}

	/**
	 * function which extracts links form web page
	 * 
	 * @param2 String: Web page
	 * @param3 String: URL of the page to be crawled
	 */
	public void extractImageLinks(String page, String url) {
		String rawPage = page;
		Log.v("extractLinks", url);
		int index = 0;
		while ((index = page.indexOf("<img ", index)) != -1) {
			int tagEnd = page.indexOf(">", index);
			if ((index = page.indexOf("src", index)) == -1)
				break;
			if ((index = page.indexOf("=", index)) == -1)
				break;
			int endTag = page.indexOf("</img", index);
			String remaining = rawPage.substring(++index);
			StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\"'>#");
			String strLink = st.nextToken();

			if ((TextUtils.equals(strLink, "javascript:void(0)"))
					|| (TextUtils.equals(strLink, "javascript:void(0);"))) {
				// avoiding javscript calls
				continue;
			}
			if (!checkLinks(strLink)) {
				strLink = makeAbsoluteURL(url, strLink);
			}
			if (!TextUtils.isEmpty(strLink)) {
				addLink(strLink);
			}
		}
		report.spiderURLProcessed(URLPool.getInstance()
				.getUrlProcessedPoolSize());
	}

	/**
	 * function which extracts links form web page
	 * 
	 * @param2 String: Web page
	 * @param3 String: URL of the page to be crawled
	 */
	public void extractImageLinks2(String page, String url) {
		String rawPage = page;
		String alternateText = "";
		String searchString = crawlPreferences.getmSearchString();
		float accuracy = 0.0f;
		Log.v("extractImageLinks2", url);
		int index = 0;
		while ((index = page.indexOf("<img ", index)) != -1) {
			if ((index = page.indexOf("src", index)) == -1)
				break;
			if ((index = page.indexOf("=", index)) == -1)
				break;

			String remaining = rawPage.substring(++index);
			StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\"'>#");
			String strLink = st.nextToken();
			
			// validate for URL completeness
			if (!checkLinks(strLink)) {
				strLink = makeAbsoluteURL(url, strLink);
			}
			
			// avoiding javaScript calls
			if ((TextUtils.equals(strLink, "javascript:void(0)"))
					|| (TextUtils.equals(strLink, "javascript:void(0);"))) {
				continue;
			}
			
			// search string validation and other filters commented
			/*if((!TextUtils.isEmpty(crawlPreferences.getmSearchString())) && (!searchStringValidation(index, rawPage))){
				continue;
			}else if((crawlPreferences.getmFileSize() != -1) && (!sizeValidation(strLink))){
					continue;
				}*/

			
			Log.v("test 5", "all test passed");
			
			if (!TextUtils.isEmpty(strLink)) {
				addLink(strLink);
			}
		}
		/*report.spiderURLProcessed(URLPool.getInstance()
				.getUrlProcessedPoolSize());*/
	}

	
	private boolean searchStringValidation(int index, String rawPage){
		boolean isSuccess = false;
		String searchString = crawlPreferences.getmSearchString();
		String alternateText = "";
		float accuracy = -1.0f;
			Log.v("test 1", "string present");
			alternateText = getAlternateText(index, rawPage);
			if (!TextUtils.isEmpty(alternateText)) {
				Log.v("test 2", "alt text present");
				if(((accuracy = getMatchAccuracy(searchString, alternateText)) != -1) || (accuracy != 0.0)){
					// accuracy match
					isSuccess = true;
				}
		}
		return isSuccess;
	}
	
	private boolean sizeValidation(String strLink){
		boolean isSuccess = false;
		
			mRequest = new HTTPRequest(strLink);
			float mediaSize = mRequest.getMediaContentLength(strLink, false);
			if((mediaSize >= crawlPreferences.getmFileSize())){
				isSuccess = true;
			}else{
				// size not matching as per the preferences move forward to next link
				Log.e("test 4", "size test failed");
			}
		
		return isSuccess;
	}
	
	/**
	 * function returning search filter settings for domain specific search
	 * 
	 * @param String: url which is being crawled
	 * @return boolean: returns true if domain filter is set and current url is from same domain
	 * 
	*/
	private boolean searchTypeCheck(String url){
		boolean isSuccess = false;
		if((crawlPreferences.ismIsDomainSpecific())&&(isFromSameDomain(url))){
			System.out.println(" is true -----------");
			isSuccess = true;
		}
		return isSuccess;
	}
	
	// TODO
	/*private boolean fileTypeValidation(){
		boolean isSuccess = false;
		
		return isSuccess;
	}*/
	
	

	/* Since it is media link ( <img> )check for match from title value or hyper link
	 * text and if image is used as Hyperlink text check that image tag
	 * alternate value
	 * @param 1: integer index of found link 
	 * @param 2: String web page being crawled
	 * @param 3: int 
	 * @return : boolean returns true if match success false otherwise
	*/
	private boolean verifyTitleTag(int index, String page, int tagEnd, int endTag) {
		int titleTagStart = 0;
		int titleTagEnd = 0;
		String titleText;
		String hyperText;
		boolean isMatch = false;
		if(((index = page.indexOf("title", index)) != -1) && ((titleTagStart = page.indexOf("=", index)) != -1)){
			String remaining = page.substring(++titleTagStart);
			StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\"'>#");
			titleText = st.nextToken();
			if((!TextUtils.isEmpty(titleText)) && (titleText.contains(crawlPreferences.getmSearchString()))){
				System.out.println(" verifyTitleTag ===title tag========"+titleText);
				isMatch = true;
			}
			
		}else {
			// check for hyper link text
			hyperText = page.substring(tagEnd + 1, endTag);
			if((!TextUtils.isEmpty(hyperText))&&(hyperText.contains("<img"))&&(!TextUtils.isEmpty(getImageAltText(hyperText)))){
				if(getImageAltText(hyperText).contains(crawlPreferences.getmSearchString())){
					System.out.println(" verifyTitleTag ===image tag========"+getImageAltText(hyperText));
					isMatch = true;
				}
			}else if((!TextUtils.isEmpty(hyperText))&&(hyperText.contains(crawlPreferences.getmSearchString()))){
				System.out.println(" verifyTitleTag ======simple link text====="+hyperText);
				isMatch = true;
			}
		}
		return isMatch;
	}

	private String getImageAltText(String page) {
		int index = 0;
		int altTagStart = 0;
		int altTagEnd = 0;
		String alternateText = "";
		if (((index = page.indexOf("<img ", index)) != -1)
				&& ((index = page.indexOf("src", index)) != -1)
				&& ((index = page.indexOf("=", index)) != -1)
				&& ((index = page.indexOf("alt", index)) != -1)
				&& ((altTagStart = page.indexOf("=", index)) != -1)) {
			String remaining = page.substring(++altTagStart);
			StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\"'>#");
			alternateText = st.nextToken();
		}

		return alternateText;
	}

	/**
	 * Return alternate text after performing series of null check validation for alt tag 
	*/
	private String getAlternateText(int index, String page) {
		String alternateText = "";
		if ((isAltTag(index, page))&& (!TextUtils.isEmpty(hasAltText(index, page)))) {
			alternateText = hasAltText(index, page);
		}
		return alternateText;
	}

	/**
	 * Returns true if alternate tag is present false otherwise
	*/
	private boolean isAltTag(int index, String page) {
		boolean isPresent = false;
		int altTagStart = 0;
		if ((altTagStart = page.indexOf("alt=\"", index)) == -1) {
			return false;
		} else {
			isPresent = true;
		}
		return isPresent;
	}

	/**
	 * Returns String representing alternate text if any present or random text otherwise which is to yet optimized to return null 
	*/
	private String hasAltText(int index, String page) {
		int altTagStart = 0;
		int altTagEnd = 0;
		String alternateText = "";
		index = page.indexOf("alt", index);
		altTagStart = page.indexOf("=", index);
		
		String remaining = page.substring(++altTagStart);
		StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\"'>#");
		alternateText = st.nextToken();
		return alternateText;
	}

	private void addLink(String url) {
		if (!ApplicationEx.operationsQueue.isShutdown()) {
			if ((URLPool.getInstance().push(url))
					&& (!URLPool.getInstance().hasPoolSizeReached())) {
				report.spiderFoundURL(url);
				report.spiderLinkCounts(URLPool.getInstance().getUrlPoolSize());
			}
		}
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
		} catch (IOException e) {
			e.printStackTrace();
			islink = false;
		}
		return islink;
	}

	/**
	 * function which validate links if it is relative links returns false for
	 * further processing else true
	 * 
	 * @param1 String: URL
	 * 
	 */
	private boolean checkLinks(String links) {
		if (links.startsWith("mailto:"))
			return false;
		if (links.startsWith("http://"))
			return true;
		if (links.startsWith("https://"))
			return true;
		/*if (links.startsWith("ftp://"))
			return true;
		if (links.startsWith("news://"))
			return true;*/
		return false;
	}

	/**
	 * function which constructs complete URL
	 * 
	 * @param1 String: base URL
	 * @param2 String: relative URL
	 */
	private String makeAbsoluteURL(String base, String relativeURL) {
		URL url = null;
		String stringURL = null;
		// Log.d(base, relativeURL);
		try {
			url = new URL(base);
			url = new URL(url, relativeURL);
			stringURL = url.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		//	Log.e("makeAbsoluteURL", e.getLocalizedMessage());
		}

		return stringURL;
	}
	
	
	/**
	 * Returns matching percentage
	 * @param 1 String: User entered Search string
	 * @param 2 String: Link Text found
	 * @return float: matching percentage
	 * 
	*/
	private float getMatchAccuracy(String searchString, String tagString){
		Log.e(searchString, tagString);
		String[] array1 = null;
		String[] array2 = null;
		float accuracy = -1f;
		try{
		String[] searchStrArray = searchString.split("[\\s,]"); 
		String[] tagArray = tagString.split("[\\s,]");
		float totalMatchCount = searchStrArray.length * tagArray.length;
		Log.e(" = "+searchStrArray.length, " + "+tagArray.length);
		float actualMatch = 0.0f;
		if(searchStrArray.length > tagArray.length){
			array1 = searchStrArray;
			array2 = tagArray;
		}else{
			array1 = tagArray;
			array2 = searchStrArray;
		}
		
		for(int i=0;i < array1.length;i++){
			for(int j=0 ;j<array2.length;j++){
				//Log.v(array1[i], array2[j]);
				if(TextUtils.equals(array1[i], array2[j])){
					actualMatch++;
				}
			}		
		}		
		System.out.println(" actual match =="+actualMatch);
		accuracy = ((actualMatch / totalMatchCount) * 100);
		}catch (Exception e) {
			e.printStackTrace();
			Log.e("Error", "getMatchAccuracy");
		}
		Log.v("accuracy", "is :"+accuracy);
		return accuracy;
	}

}
