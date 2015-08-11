package com.peony.crawler.model;

public class Config {

	private String url;
	/**
	 * 万能的正则，可以为空
	 */
	private String regex;

	/**
	 * json or xml
	 */
	private ResponseType responseType;

	/**
	 * 数据根节点xpath:/DOCUMENT/
	 */
	private String rootNode;

	/**
	 * /DOCUMENT/item/display/title/
	 */
	private String titlePath;

	/**
	 * /DOCUMENT/item/display/date/
	 */
	private String timePath;

	private String urlPath;

	private String contentPath;

	public Config(String url, String regex, ResponseType responseType) {
		super();
		this.url = url;
		this.regex = regex;
		this.responseType = responseType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	public String getRootNode() {
		return rootNode;
	}

	public void setRootNode(String rootNode) {
		this.rootNode = rootNode;
	}

	public String getTitlePath() {
		return titlePath;
	}

	public void setTitlePath(String titlePath) {
		this.titlePath = titlePath;
	}

	public String getTimePath() {
		return timePath;
	}

	public void setTimePath(String timePath) {
		this.timePath = timePath;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(String contentPath) {
		this.contentPath = contentPath;
	}
}
