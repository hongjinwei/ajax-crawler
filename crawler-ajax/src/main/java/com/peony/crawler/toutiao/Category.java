package com.peony.crawler.toutiao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Category {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Category.class);

	private static String[] category = { 
		    "news_hot",      //热点
		    "news_society", // 社会
			"news_entertainment", // 娱乐
			"news_tech", // 科技
			"news_car", // 汽车
			"news_sports", // 体育
			"news_finance", // 财经
			"news_military", // 体育
			"news_world", // 国际
			"news_fashion", // 时尚
			"news_game", // 游戏
			"news_travel", // 旅游
			"news_history", // 历史
			"news_discovery", // 探索
			"news_food", // 美食
			"news_baby", // 育儿
			"news_regimen", // 养生
			"news_story", // 故事
			"news_essay" // 美文
	};

	private static List<String> categories = new ArrayList<String>(
			Arrays.asList(category));

	private static int count = 1;
	private static int start = 0;
	private static int end = 0;
	private static int threadNumber = 1;
	private static int len = category.length;

	public static boolean init(int number) {
		if (number <= 0) {
			LOGGER.error("线程数目参数不能小于或等于0！将自动修改为1");
			number = 1;
		}
		threadNumber = (int) number;
		count = 1;
		return true;
	}

	synchronized public static List<String> getCategories() {
		start = end;
		if (count == threadNumber) {
			end = len;
		} else {
			end = start + len / threadNumber;
		}

		List<String> results = categories.subList(start, end);

		count++;
		if (count > threadNumber) {
			count = 1;
			start = end = 0;
		}
		return results;
	}
}
