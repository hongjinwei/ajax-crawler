package com.peony.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.peony.crawler.model.Config;
import com.peony.crawler.model.ResponseType;
import com.peony.util.StringUtils;
import com.peony.util.http.BaseHttpException;
import com.peony.util.http.HttpQuery;

public class App {

	static HttpQuery browser = HttpQuery.getInstance();

	public static void main(String[] args) throws BaseHttpException {
		Config c = new Config(
				"http://weixin.sogou.com/gzhjs?cb=sogou.weixin.gzhcb&eqs=iisgoGBghrVaoMoRrWUnJupQSoI6L6EmH%2FwHVDXikFfhXlMihLVspk%2FzVakysuBJdCdKI&ekv=1&page=1&t=1432517339107",
				"(\\{.*\\})", ResponseType.JSON);

		String html = browser.get(c.getUrl()).asString();
		String content = StringUtils.match(html, c.getRegex()).get(0);
		if (c.getResponseType().equals(ResponseType.JSON)) {
			JSONObject o = JSON.parseObject(content);
			JSONArray array = o.getJSONArray("items");
			System.out.println(array.size());
		} else if (c.getResponseType().equals(ResponseType.XML)) {

		}
	}
}
