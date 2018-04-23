package com.kettle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.kettle.bean.KettleJobEntireDefine;
import com.kettle.bean.KettleResult;
import com.kettle.consist.KettleVariables;
import com.kettle.meta.KettleTableMeta;
import com.kettle.meta.builder.TableDataMigrationBuilder;
import com.kettle.service.KettleNorthService;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@EnableScheduling
@EnableTransactionManagement
public class Application extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(Application.class);
		app.addListeners(new ApplicationEnvironmentPreparedEventListener());
		app.addListeners(new ApplicationFailedEventListener());
		app.addListeners(new ApplicationPreparedEventListener());
		app.addListeners(new ApplicationStartedEventListener());
		ConfigurableApplicationContext context = app.run(args);
		// 测试

		KettleNorthService service = context.getBean(KettleNorthService.class);
		System.out.println("------------------------------");
//		List<String> flags = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
//				"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
		 List<String> flags = Arrays.asList("A");
		KettleTableMeta source = null;
		KettleTableMeta target = null;
		List<KettleJobEntireDefine> kettleJobEntireDefines = new ArrayList<KettleJobEntireDefine>();
		for (String flag : flags) {
			// 源配置
			source = new KettleTableMeta();
			source.setType("MySQL");
			source.setHost("192.168.80.138");
			source.setPort("3306");
			source.setDatabase("employees");
			source.setUser("root");
			source.setPasswd("123456");
			if ("".equals(flag)) {
				source.setSql(
						"SELECT employees.emp_no, dept_emp.dept_no, employees.first_name, employees.last_name, employees.birth_date "
								+ "FROM employees, dept_emp WHERE employees.emp_no = dept_emp.emp_no");
			} else {
				source.setSql(
						"SELECT employees.emp_no, dept_emp.dept_no, employees.first_name, employees.last_name, employees.birth_date "
								+ "FROM employees, dept_emp WHERE employees.emp_no = dept_emp.emp_no AND first_name LIKE '"
								+ flag + "%'");
			}
			source.setColumns(Arrays.asList("emp_no", "dept_no", "first_name", "last_name", "birth_date"));
			source.setPkcolumns(Arrays.asList("emp_no", "dept_no"));
			// 目标配置
			target = new KettleTableMeta();
			target.setType("MySQL");
			target.setHost("192.168.80.138");
			target.setPort("3306");
			target.setDatabase("person");
			target.setUser("root");
			target.setPasswd("123456");
			target.setColumns(Arrays.asList("empID", "deptID", "firstName", "lastName", "born"));
			target.setPkcolumns(Arrays.asList("empID", "deptID"));
			if ("".equals(flag)) {
				target.setSql("SELECT empID, deptID, firstName, lastName, born FROM target_employees");
				target.setTableName("target_employees");
			} else {
				target.setSql("SELECT empID, deptID, firstName, lastName, born FROM target_employees_" + flag);
				target.setTableName("target_employees_" + flag);
			}
			kettleJobEntireDefines.add(TableDataMigrationBuilder.newBuilder().source(source).target(target).createJob());
		}
		// 注册
		List<KettleResult> kettleResults = new ArrayList<KettleResult>();
		for (KettleJobEntireDefine kettleJobEntireDefine : kettleJobEntireDefines) {
			kettleResults.add(service.registeJob(kettleJobEntireDefine));
		}
		for (KettleResult result : kettleResults) {
			service.deleteJob(result.getUuid());
		}
		kettleResults.clear();
		for (KettleJobEntireDefine kettleJobEntireDefine : kettleJobEntireDefines) {
			kettleResults.add(service.excuteJobOnce(kettleJobEntireDefine));
		}
//		KettleResult roll;
//		KettleResult result;
//		while (!kettleResults.isEmpty()) {
//			Thread.sleep(5000);
//			for (Iterator<KettleResult> it = kettleResults.iterator(); it.hasNext();) {
//				roll = it.next();
//				result = service.queryJob(roll.getUuid());
//				if (KettleVariables.RECORD_STATUS_ERROR.equals(result.getStatus())
//						|| KettleVariables.RECORD_STATUS_FINISHED.equals(result.getStatus())) {
//					it.remove();
//				}
//			}
//		}
	}

}
