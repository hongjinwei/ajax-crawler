package com.peony.crawler.model;

/**
 * http响应结果类型，对于ajax请求的数据响应结果，目前发现的只有json和xml两种格式
 * 
 * @author guor
 */
public enum ResponseType {

	JSON(1, "json"), XML(2, "xml");
	private int id;
	private String name;

	private ResponseType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}