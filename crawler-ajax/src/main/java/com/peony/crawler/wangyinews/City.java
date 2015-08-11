package com.peony.crawler.wangyinews;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class City {

	// private static String[] city = { "北京", "上海", "武汉", "杭州", "深圳", "宁波",
	// "合肥", "石家庄", "云南" , "天津"};
	private static int threadNumber = 1;
	// private static List<String> cityname = new
	// ArrayList<String>(Arrays.asList(city));
	private static List<String> cityname = new ArrayList<String>();
	private static int count = 1;
	private static int start = 0;
	private static int end = 0;
	private static final String confFile = "/city.conf";
	private static final Logger LOGGER = LoggerFactory.getLogger(WangyiCrawlerTask.class);

	private static void setThreadNumber(int number) {
		threadNumber = (number <= 0)? 1 : number ;
	}

	/**
	 * 得到所有城市的列表,初始化成功返回true，否则返回false
	 */
	public static boolean init(int threadNumber) {
		setThreadNumber(threadNumber);
		count = 1;
		boolean flag = false;
		int tryCount = 0;

		while (!flag && tryCount < 3) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(City.class.getResourceAsStream(confFile)));
				try {
					String in;
					while ((in = br.readLine()) != null && !in.equals("")) {
						cityname.add(in);
					}
					flag = true;
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				} finally {
					br.close();
				}
			} catch (Exception e) {
				tryCount++;
				LOGGER.error(e.getMessage(), e);
			}
		}

		return flag;
	}

	public synchronized static List<String> getCityNames() {
		start = end;

		if (count == threadNumber) {
			end = cityname.size();
		} else {
			end = start + cityname.size() / threadNumber;
		}

		List<String> subList = cityname.subList(start, end);
		count++;
		if (count > threadNumber) {
			start = 0;
			end = 0;
			count = 1;
		}
		return subList;
	}
}
