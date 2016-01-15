package com.peony.crawler.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.spi.DirStateFactory.Result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.peony.crawler.ConnectionManager;
import com.peony.crawler.model.ServerInitException;
import com.peony.util.StringUtils;
import com.peony.util.http.HttpQuery;

public class TestToutiao {

	private static final String[] category = { "news_hot", // 热点
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
	private static HttpQuery browser = HttpQuery.newInstance();

	private static final String jsonPage = "http://toutiao.com/api/article/recent/?source=2&count=%d&category=%s&utm_source=toutiao";

	private static String createUrl(String category) {
		String url = String.format(jsonPage, 20, category);
		return url;
	}

	private static String handleSahreUrl(String url) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < url.length()) {
			if (url.charAt(i) != '?') {
				sb.append(url.charAt(i));
			} else {
				break;
			}
			i++;
		}
		return sb.toString();
	}

	private static List<String> extractURL(String url) throws Exception {
		List<String> ans = new ArrayList<String>();
		String data = browser.get(url).asString();
		JSONObject obj = JSONObject.parseObject(data);
		JSONArray array = obj.getJSONArray("data");
		for (int i = 0; i < array.size(); i++) {
			JSONObject item = array.getJSONObject(i);
			System.out.println(item.toString());
			String pageUrl = item.getString("share_url");
			System.out.println(handleSahreUrl(pageUrl));
			ans.add(pageUrl);
		}
		return ans;
	}

	private static void query(List<String> urls) {
		ConnectionManager manager = ConnectionManager.getInstance();
		int exist = 0;
		try {
			manager.init();
			Connection conn = manager.getConnection();

			for (String url : urls) {
				try {
					PreparedStatement ps = conn.prepareStatement("select * from wdyq_pages where id=?");
					ps.setString(1, StringUtils.MD5(url));
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						exist++;
						System.out.println("exist:" + url);
					} else {
						System.out.println("not  :" + url);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println((double) exist / urls.size());
		} catch (ServerInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		query(extractURL(createUrl(category[1])));

	}
}
