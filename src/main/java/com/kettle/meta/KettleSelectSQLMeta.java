package com.kettle.meta;

/**
 * Kettle的SQL元数据定义
 * 
 * @author Administrator
 *
 */
public class KettleSelectSQLMeta extends KettleDBMeta {
	/**
	 * 执行的SQL
	 */
	private String sql;

	private String[] conlumns;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String[] getConlumns() {
		return conlumns;
	}

	public void setConlumns(String[] conlumns) {
		this.conlumns = conlumns;
	}
}
