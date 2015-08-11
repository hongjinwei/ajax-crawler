package com.peony.crawler;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.peony.crawler.model.ServerInitException;
import com.peony.util.StringUtils;
import com.peony.util.base.Singleton;

@Singleton
public class ConnectionManager {

	private static ComboPooledDataSource dataSource;

	/**
	 * 日志管理对象
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConnectionManager.class);

	/**
	 * 数据库连接池配置文件名常量
	 */
	private static final String JDBC_CONFIG = "/c3p0.properties";

	/**
	 * 驱动参数常量
	 */
	private static final String DRIVER_PROP = "driver";

	/**
	 * 连接参数常量
	 */
	private static final String URL_PROP = "url";

	/**
	 * 用户名参数常量
	 */
	private static final String USERNAME_PROP = "username";

	/**
	 * 密码参数常量
	 */
	private static final String PASSWORD_PROP = "password";

	/**
	 * 获取连接时，最大等待时间参数常量
	 */
	private static final String MAXWAIT_PROP = "maxWait";

	/**
	 * 获取连接时，默认最大等待时间
	 */
	private static final int DEFAULT_MAXWAIT_TIME = 5000;

	/**
	 * {@inheritDoc}
	 */
	public void init() throws ServerInitException {
		try {
			LOGGER.info("正在初始化数据库连接池...");
			Properties prop = loadJdbcProperties();
			dataSource = new ComboPooledDataSource();
			dataSource.setDriverClass(prop.getProperty(DRIVER_PROP));
			dataSource.setJdbcUrl(prop.getProperty(URL_PROP));
			dataSource.setUser(prop.getProperty(USERNAME_PROP));
			dataSource.setPassword(prop.getProperty(PASSWORD_PROP));
			dataSource.setCheckoutTimeout(StringUtils.parseInt(
					prop.getProperty(MAXWAIT_PROP), DEFAULT_MAXWAIT_TIME));
			dataSource.setMaxPoolSize(30);
			dataSource.setInitialPoolSize(10);
		
			testConnection(dataSource);
			LOGGER.info("初始化数据库连接池成功！");
		} catch (IOException e) {
			throw new ServerInitException("初始化数据库连接池失败！", e);
		} catch (PropertyVetoException e) {
			throw new ServerInitException("初始化数据库连接池失败！", e);
		}
	}

	/**
	 * 获取连接
	 * 
	 * @return
	 */
	public synchronized final Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void finalize() throws Throwable {
		DataSources.destroy(dataSource); // 关闭datasource
		super.finalize();
	}

	/**
	 * 测试数据库连接池
	 */
	private static void testConnection(ComboPooledDataSource dataSource)
			throws ServerInitException {
		try {
			Connection connection = dataSource.getConnection();
			connection.close();
		} catch (SQLException e) {
			throw new ServerInitException("初始化数据库连接池失败！", e);
		}
	}

	private Properties loadJdbcProperties() throws IOException {
		InputStream in;
		if (SystemProps.isTest()) {
			LOGGER.info("使用测试c3p0配置文件 test_c3p0.properties ！");
			in = this.getClass().getResourceAsStream(("/test_c3p0.properties"));
		} else {
			in = this.getClass().getResourceAsStream((JDBC_CONFIG));
		}
		try {
			Properties prop = new Properties();
			prop.load(in);
			return prop;
		} finally {
			in.close();
		}
	}
}
