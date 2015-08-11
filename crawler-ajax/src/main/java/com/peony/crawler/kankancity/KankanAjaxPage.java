package com.peony.crawler.kankancity;

public class KankanAjaxPage {

	private String region = "null";
	private String city = "null";
	private String url = "null";
	private String domain = "null";

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getUrl() {
		return url + "?t=" + System.currentTimeMillis();
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toString() {
		return region + " " + city + " " + domain + " " + url;
	}

}
