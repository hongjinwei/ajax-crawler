package com.peony.crawler;

import com.avaje.ebean.Ebean;
import com.peony.crawler.model.WebPage;

public class WebPageRepository {

	public static void save(WebPage page) {
		Ebean.save(page);
	}
}
