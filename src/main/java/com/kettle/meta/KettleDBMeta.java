package com.kettle.meta;

/**
 * 数据库元数据
 * 
 * @author Administrator
 *
 */
public class KettleDBMeta {

	/**
	 * 数据库类别
	 */
	private String type;

	/**
	 * 数据库IP
	 */
	private String host;

	/**
	 * 端口
	 */
	private String port;

	/**
	 * DataBase
	 */
	private String database;

	/**
	 * 数据库登入用户
	 */
	private String user;

	/**
	 * 数据库登入密码
	 */
	private String passwd;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
}
