package com.peony.crawler.model;


/**
 * 页面解析之后的结果包装类
 * @author BAO
 */
public class ParseResult {

	private String content;
	private WebPage page;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public WebPage getPage() {
		return page;
	}
	public void setPage(WebPage page) {
		this.page = page;
	}
}
