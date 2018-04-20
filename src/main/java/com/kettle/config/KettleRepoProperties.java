package com.kettle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * kettle的资源配置信息
 * 
 * @author Administrator
 *
 */
@Component
@ConfigurationProperties(prefix = "kettle.repository")
@PropertySource("classpath:/config/kettle_env.properties")
public class KettleRepoProperties {
	/**
	 * ID
	 */
	private String id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 描述
	 */
	private String description;

	/**
	 * 基础路径
	 */
	private String baseDirectory;

	/**
	 * User
	 */
	private String user;

	/**
	 * 密码
	 */
	private String passwd;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
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
