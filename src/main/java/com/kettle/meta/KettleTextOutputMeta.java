package com.kettle.meta;

public class KettleTextOutputMeta {

	/**
	 * 行数据分隔符
	 */
	private String separator = ";";

	/**
	 * 后缀
	 */
	private String extension = "";

	/**
	 * 文件名称(包含完整路径)
	 */
	private String exportFileName = "file";
	
	/**
	 * 文件头
	 */
	private boolean headerEnabled = false;
	
	/**
	 * 文件尾
	 */
	private boolean footerEnabled = false;

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getExportFileName() {
	    return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
	    this.exportFileName = exportFileName;
	}

	public boolean isHeaderEnabled() {
		return headerEnabled;
	}

	public void setHeaderEnabled(boolean headerEnabled) {
		this.headerEnabled = headerEnabled;
	}

	public boolean isFooterEnabled() {
		return footerEnabled;
	}

	public void setFooterEnabled(boolean footerEnabled) {
		this.footerEnabled = footerEnabled;
	}
}
