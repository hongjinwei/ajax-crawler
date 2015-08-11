package com.peony.crawler;

import com.peony.util.StringUtils;

/**
 * 系统属性，可以通过虚拟机参数设置全局参数
 * @author guor
 * @date 2015年3月15日上午9:47:16
 */
public class SystemProps {

	public static final String LOG_ENABLED = "log";

	public static final String CACHEABLE = "cacheable";

	public static final String STOREABLE = "storeable";
	
	public static final String STOREURL = "storeurl";
	
	public static final String TEST = "test";
	
	public static boolean isTest(){
		String property = System.getProperty(TEST);
		if (StringUtils.isEmpty(property)) {
			return false;// 默认不是test
		}
		return StringUtils.parseBoolean(property, true);
	}
	
	public static boolean storeurl() {
		String property = System.getProperty(STOREURL);
		if (StringUtils.isEmpty(property)) {
			return true;// 默认保存url
		}
		return StringUtils.parseBoolean(property, true);
	}
	
	public static boolean storeable() {
		String property = System.getProperty(STOREABLE);
		if (StringUtils.isEmpty(property)) {
			return true;// 默认使用缓存
		}
		return StringUtils.parseBoolean(property, true);
	}
	
	public static boolean logEnabled() {
		String property = System.getProperty(LOG_ENABLED);
		if (StringUtils.isEmpty(property)) {
			return true;// 默认记日志
		}
		return StringUtils.parseBoolean(property, true);
	}

	public static boolean cacheable() {
		String property = System.getProperty(CACHEABLE);
		if (StringUtils.isEmpty(property)) {
			return true;// 默认使用缓存
		}
		return StringUtils.parseBoolean(property, true);
	}
}
