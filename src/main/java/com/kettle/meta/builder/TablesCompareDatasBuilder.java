package com.kettle.meta.builder;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.mergerows.MergeRowsMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.sql.ExecSQLMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import com.kettle.bean.KettleJobEntireDefine;
import com.kettle.meta.KettleTableMeta;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * Table比较构建器
 * 
 * @author Administrator
 *
 */
public class TablesCompareDatasBuilder {
	/**
	 * 基础
	 */
	private KettleTableMeta base;
	/**
	 * 对比者
	 */
	private KettleTableMeta compare;
	/**
	 * 新操作
	 */
	private KettleTableMeta newOption;

	private TablesCompareDatasBuilder() {
	}

	public static TablesCompareDatasBuilder newBuilder() {
		return new TablesCompareDatasBuilder();
	}

	public TablesCompareDatasBuilder base(KettleTableMeta base) {
		this.base = base;
		return this;
	}

	public TablesCompareDatasBuilder compare(KettleTableMeta compare) {
		this.compare = compare;
		return this;
	}

	public TablesCompareDatasBuilder newOption(KettleTableMeta newOption) {
		this.newOption = newOption;
		return this;
	}

	public TransMeta createTrans() throws Exception {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		Select baseSelect = (Select) CCJSqlParserUtil.parse(base.getSql());
		Select compareSelect = (Select) CCJSqlParserUtil.parse(compare.getSql());
		TransMeta transMeta = null;
		transMeta = new TransMeta();
		transMeta.setName("CTD-" + uuid);
		DatabaseMeta baseDataBase = new DatabaseMeta(base.getHost() + "_" + base.getDatabase() + "_" + base.getUser(),
				base.getType(), "Native", base.getHost(), base.getDatabase(), base.getPort(), base.getUser(),
				base.getPasswd());
		if ("ORACLE".equals(base.getType())) {
			String oracle = PluginRegistry.getInstance().getPlugin(DatabasePluginType.class, "ORACLE").getIds()[0];
			baseDataBase.addExtraOption(oracle, "QTO", "F");
		}
		transMeta.addDatabase(baseDataBase);
		DatabaseMeta compareDatabase = new DatabaseMeta(
				compare.getHost() + "_" + compare.getDatabase() + "_" + compare.getUser(), compare.getType(), "Native",
				compare.getHost(), compare.getDatabase(), compare.getPort(), compare.getUser(), compare.getPasswd());
		if ("ORACLE".equals(compare.getType())) {
			String oracle = PluginRegistry.getInstance().getPlugin(DatabasePluginType.class, "ORACLE").getIds()[0];
			compareDatabase.addExtraOption(oracle, "QTO", "F");
		}
		transMeta.addDatabase(compareDatabase);
		DatabaseMeta newDatabase = new DatabaseMeta(
				newOption.getHost() + "_" + newOption.getDatabase() + "_" + newOption.getUser(), newOption.getType(),
				"Native", newOption.getHost(), newOption.getDatabase(), newOption.getPort(), newOption.getUser(),
				newOption.getPasswd());
		if ("ORACLE".equals(newOption.getType())) {
			String oracle = PluginRegistry.getInstance().getPlugin(DatabasePluginType.class, "ORACLE").getIds()[0];
			newDatabase.addExtraOption(oracle, "QTO", "F");
		}
		transMeta.addDatabase(newDatabase);
		/*
		 * 条件
		 */
		String[] conditions = new String[compare.getPkcolumns().size()];
		for (int i = 0; i < conditions.length; i++) {
			conditions[i] = "=";
		}
		/*
		 * Note
		 */
		String startNote = "Start " + transMeta.getName();
		NotePadMeta ni = new NotePadMeta(startNote, 150, 10, -1, -1);
		transMeta.addNote(ni);
		/*
		 * base
		 */
		TableInputMeta tii = new TableInputMeta();
		tii.setDatabaseMeta(baseDataBase);
		List<OrderByElement> selectOrderBy = ((PlainSelect) baseSelect.getSelectBody()).getOrderByElements();
		if (selectOrderBy == null) {
			selectOrderBy = new LinkedList<OrderByElement>();
			((PlainSelect) baseSelect.getSelectBody()).setOrderByElements(selectOrderBy);
		}
		selectOrderBy.clear();
		OrderByElement orderByElement = null;
		for (String pk : base.getPkcolumns()) {
			orderByElement = new OrderByElement();
			orderByElement.setExpression(new Column(pk));
			selectOrderBy.add(orderByElement);
		}
		tii.setSQL(baseSelect.getSelectBody().toString());
		StepMeta query = new StepMeta("base", tii);
		query.setLocation(150, 100);
		query.setDraw(true);
		query.setDescription("STEP-BASE");
		transMeta.addStep(query);

		/*
		 * 转换名称
		 */
		String[] baseFields = base.getColumns().toArray(new String[0]);
		String[] compareFields = compare.getColumns() == null ? baseFields
				: compare.getColumns().toArray(new String[0]);
		int[] comparePrecisions = new int[baseFields.length];
		int[] compareLengths = new int[compareFields.length];
		SelectValuesMeta svi = new SelectValuesMeta();
		svi.setSelectLength(compareLengths);
		svi.setSelectPrecision(comparePrecisions);
		svi.setSelectName(baseFields);
		svi.setSelectRename(compareFields);
		svi.setDeleteName(new String[0]);
		svi.setMeta(new SelectMetadataChange[0]);
		StepMeta chose = new StepMeta("chose", svi);
		chose.setLocation(350, 100);
		chose.setDraw(true);
		chose.setDescription("STEP-CHOSE");
		transMeta.addStep(chose);
		transMeta.addTransHop(new TransHopMeta(query, chose));

		/*
		 * compare
		 */
		TableInputMeta comparetii = new TableInputMeta();
		comparetii.setDatabaseMeta(compareDatabase);
		selectOrderBy = ((PlainSelect) compareSelect.getSelectBody()).getOrderByElements();
		if (selectOrderBy == null) {
			selectOrderBy = new LinkedList<OrderByElement>();
			((PlainSelect) compareSelect.getSelectBody()).setOrderByElements(selectOrderBy);
		}
		selectOrderBy.clear();
		for (String pk : compare.getPkcolumns()) {
			orderByElement = new OrderByElement();
			orderByElement.setExpression(new Column(pk));
			selectOrderBy.add(orderByElement);
		}
		comparetii.setSQL(compareSelect.getSelectBody().toString());
		StepMeta compareQuery = new StepMeta("compare", comparetii);
		transMeta.addStep(compareQuery);
		compareQuery.setLocation(350, 300);
		compareQuery.setDraw(true);
		compareQuery.setDescription("STEP-COMPARE");

		/*
		 * merage
		 */
		MergeRowsMeta mrm = new MergeRowsMeta();
		mrm.setFlagField("flagfield");
		mrm.setValueFields(new String[0]);
		mrm.setKeyFields(compare.getPkcolumns().toArray(new String[0]));
		mrm.getStepIOMeta().setInfoSteps(new StepMeta[] { compareQuery, chose });
		StepMeta merage = new StepMeta("merage", mrm);
		transMeta.addStep(merage);
		merage.setLocation(650, 100);
		merage.setDraw(true);
		merage.setDescription("STEP-MERAGE");
		transMeta.addTransHop(new TransHopMeta(chose, merage));
		transMeta.addTransHop(new TransHopMeta(compareQuery, merage));
		/*
		 * isNew
		 */
		FilterRowsMeta frm_new = new FilterRowsMeta();
		frm_new.setCondition(
				new Condition("flagfield", Condition.FUNC_EQUAL, null, new ValueMetaAndData("constant", "new")));
		StepMeta isNew = new StepMeta("isNew", frm_new);
		isNew.setLocation(950, 100);
		isNew.setDraw(true);
		isNew.setDescription("STEP-ISNEW");
		transMeta.addStep(isNew);
		transMeta.addTransHop(new TransHopMeta(merage, isNew));
		/*
		 * insert
		 */
		ExecSQLMeta esm = new ExecSQLMeta();
		esm.setDatabaseMeta(newDatabase);
		esm.setSql(newOption.getSql());
		esm.setExecutedEachInputRow(true);
		esm.setVariableReplacementActive(true);
		esm.setParams(true);
		esm.setArguments(newOption.getColumns().toArray(new String[0]));
		StepMeta sqlExcute = new StepMeta("sqlExcute", esm);
		sqlExcute.setLocation(950, 300);
		sqlExcute.setDraw(true);
		sqlExcute.setDescription("STEP-SQLEXCUTE");
		transMeta.addStep(sqlExcute);
		transMeta.addTransHop(new TransHopMeta(isNew, sqlExcute));
		frm_new.getStepIOMeta().getTargetStreams().get(0).setStepMeta(sqlExcute);
		/*
		 * nothing
		 */
		StepMeta nothing = new StepMeta("nothing", new DummyTransMeta());
		nothing.setLocation(1250, 100);
		nothing.setDraw(true);
		nothing.setDescription("STEP-NOTHING");
		transMeta.addStep(nothing);
		transMeta.addTransHop(new TransHopMeta(isNew, nothing));
		frm_new.getStepIOMeta().getTargetStreams().get(1).setStepMeta(nothing);
		return transMeta;
	}

	public KettleJobEntireDefine createJob() throws Exception {
		KettleJobEntireDefine kettleJobEntireDefine = new KettleJobEntireDefine();
		TransMeta transMeta = createTrans();
		kettleJobEntireDefine.getDependentTrans().add(transMeta);

		JobMeta mainJob = new JobMeta();
		mainJob.setName(UUID.randomUUID().toString().replace("-", ""));
		// 启动
		JobEntryCopy start = new JobEntryCopy(new JobEntrySpecial("START", true, false));
		start.setLocation(150, 100);
		start.setDrawn(true);
		start.setDescription("START");
		mainJob.addJobEntry(start);
		// 主执行
		JobEntryTrans trans = new JobEntryTrans(transMeta.getName());
		trans.setTransObjectId(transMeta.getObjectId());
		trans.setWaitingToFinish(true);
		// 当前目录,即job的同级目录
		trans.setDirectory("${Internal.Entry.Current.Directory}");
		trans.setTransname(transMeta.getName());
		JobEntryCopy excuter = new JobEntryCopy(trans);
		excuter.setLocation(300, 100);
		excuter.setDrawn(true);
		excuter.setDescription("MAINJOB");
		mainJob.addJobEntry(excuter);
		// 连接
		JobHopMeta hop = new JobHopMeta(start, excuter);
		mainJob.addJobHop(hop);
		kettleJobEntireDefine.setMainJob(mainJob);
		return kettleJobEntireDefine;
	}
}