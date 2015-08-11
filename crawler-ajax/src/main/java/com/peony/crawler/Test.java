package com.peony.crawler;

import com.peony.util.cache.CacheClient;

public class Test {

	public static void main(String[] args) {
		CacheClient cacheClient = CommonUtils.getCacheClient();
		boolean checkUrl = cacheClient.checkUrl("http://tthz.huanqiu.com/viewTouTiao.html?newId=5979598&f=jrtt");
		System.out.println(checkUrl);
	}

}
