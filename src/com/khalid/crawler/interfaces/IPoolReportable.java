package com.khalid.crawler.interfaces;


public interface IPoolReportable {
	void onURLPoolEntryAdded(String url);
	void onURLMediaLinkSaved(String savedLink);
	void onURLMediaLinkError(String errorLink);
}
