package com.kettle.consist;

/**
 * Kettle的常量
 * 
 * @author Administrator
 *
 */
public class KettleVariables {

	/**
	 * 记录的运行状态:运行中
	 */
	public static final String RECORD_STATUS_RUNNING = "RUNNING";

	/**
	 * 记录的运行状态:注册
	 */
	public static final String RECORD_STATUS_REGISTE = "REGISTE";

	/**
	 * 记录的运行状态:受理
	 */
	public static final String RECORD_STATUS_APPLY = "APPLY";

	/**
	 * 记录的运行状态:异常
	 */
	public static final String RECORD_STATUS_ERROR = "ERROR";

	/**
	 * 记录的运行状态:完成
	 */
	public static final String RECORD_STATUS_FINISHED = "FINISHED";

	/**
	 * 远端的运行状态:异常
	 */
	public static final String REMOTE_STATUS_ERROR = "ERROR";

	/**
	 * 远端的运行状态:正常
	 */
	public static final String REMOTE_STATUS_RUNNING = "Online";

	/**
	 * 历史记录的TYPE:JOB
	 */
	public static final String RECORD_TYPE_JOB = "JOB";

	/**
	 * 转换记录的元数据ID:唯一
	 */
	public static final String RECORD_TYPE_TRANS = "TRANS";

	/**
	 * 转换记录的元数据ID:唯一
	 */
	public static final String RECORD_E_TRANS = "TRANS";

	/**
	 * 转换记录的元数据ID:唯一
	 */
	public static final String RECORD_EXECUTION_TYPE_ONCE = "ONCE";

	/**
	 * 转换记录的元数据ID:唯一
	 */
	public static final String RECORD_EXECUTION_TYPE_PERSISTENT = "PERSISTENT";

	/**
	 * 转换记录的元数据ID:唯一
	 */
	public static final String RECORD_EXECUTION_TYPE_CRON = "CRON";
}
