package com.peony.crawler.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.peony.crawler.ConnectionManager;
import com.peony.util.base.New;

public class WebPageManager {

	private static WebPageManager instance;

	private WebPageManager() {
	};

	public static WebPageManager getInstance() {
		if (instance == null) {
			instance = new WebPageManager();
		}
		return instance;
	}

	private static final String INSERT_PAGE_SQL = "insert into wdyq_pages (id,url,webSite,downloadDate,title,summary,"
			+ "type,publishDate,sitePriority,indexedStatus) values (?,?,?,?,?,?,?,?,?,?)";

	private static final String INSERT_PAGEEXT_SQL = "insert into wdyq_page_ext (id,dataSource,downloadDate) values (?,?,?)";

	public void savePage(WebPage page) throws SQLException, Exception {
		Connection conn = New.getInstance(ConnectionManager.class).getConnection();
		if (conn == null) {
			throw new Exception("获取数据库连接失败！");
		}
		try {
			PreparedStatement pstmt = conn.prepareStatement(INSERT_PAGE_SQL);
			try {
				int p = 1;
				pstmt.setString(p++, page.getId());
				pstmt.setString(p++, page.getUrl());
				pstmt.setString(p++, page.getWebSite());
				pstmt.setTimestamp(p++, page.getDownloadDate());
				pstmt.setString(p++, page.getTitle());
				pstmt.setString(p++, page.getSummary());
				pstmt.setInt(p++, page.getType());
				pstmt.setTimestamp(p++, page.getPublishDate());
				pstmt.setInt(p++, page.getSitePriority());
				pstmt.setInt(p++, page.getIndexedStatus());
				int update = pstmt.executeUpdate();
			} finally {
				pstmt.close();
			}
			//
			// pstmt = conn.prepareStatement(INSERT_PAGEEXT_SQL);
			// try {
			// int p = 1;
			// pstmt.setString(p++, page.getId());
			// pstmt.setString(p++, page.getUrl());
			// pstmt.setString(p++, page.getSource());
			// pstmt.setTimestamp(p++, page.getDownloadDate());
			// } finally {
			// pstmt.close();
			// }
		} finally {
			conn.close();
		}
	}
}
