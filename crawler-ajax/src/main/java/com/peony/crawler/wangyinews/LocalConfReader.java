package com.peony.crawler.wangyinews;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 将json转化为city.conf的本地代码，线上不执行
 * @author BAO
 *
 */
public class LocalConfReader {
	
	private static String file2string(File file) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					file));
			try {
				String content;
				while ((content = bufferedReader.readLine()) != null) {
					stringBuilder.append(content);
				}
				return stringBuilder.toString();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) { 
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				bufferedReader.close();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static void readCityJson2Conf() {
		Set<String> tmp = new HashSet<String>();
		List<String> cityname = new ArrayList<String>();
		try {		
			//String filename = "C:\\Users\\BAO\\git\\crawler-ajax\\src\\main\\resources\\city.json";
			BufferedReader br = new BufferedReader(new InputStreamReader(City.class.getResourceAsStream("/city.json")));
			StringBuilder builder = new StringBuilder();
			String str = null;
			while( (str = br.readLine()) != null ){
				builder.append(str);
			}
			String json = builder.toString();
			//String json = file2string(new File(filename));
			JSONObject obj = JSONObject.parseObject(json);
			for(char i = 'A'; i <= 'Z'; i++){
				try{
					JSONArray array = obj.getJSONArray(new String(i + ""));
					for (int k = 0; k < array.size(); k++) {
						try {
							JSONObject o = array.getJSONObject(k);
							Set<String> set = o.keySet();
							tmp.addAll(set);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cityname.addAll(tmp);
		}

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(
					new FileWriter(
							"C:\\Users\\BAO\\git\\crawler-ajax\\src\\main\\resources\\city.conf",
							true));
			for (int i = 0; i < cityname.size(); i++) {
				out.write(cityname.get(i) + "\n");
			}
		} catch (IOException e) {
			// error processing code
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
