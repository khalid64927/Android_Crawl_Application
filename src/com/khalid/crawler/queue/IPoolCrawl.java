package com.khalid.crawler.queue;

public interface IPoolCrawl {

	boolean push(String url);
	String pop();
	int getSize();
}
