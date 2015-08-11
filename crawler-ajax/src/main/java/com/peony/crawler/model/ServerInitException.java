package com.peony.crawler.model;

public class ServerInitException extends Exception {
	/**
	 * 
	 * @param message 异常消息
	 */
	public ServerInitException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message 异常消息
	 * @param cause 异常堆栈
	 */
	public ServerInitException(String message, Throwable cause) {
		super(message, cause);
	}
}
