package com.kettle.config;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kettle.core.remote.KettleRemotePool;
import com.kettle.repository.KettleRepoRepository;

@Configuration
@EnableAutoConfiguration
public class KettleConfig {
	/**
	 * 日志
	 */
	private static Logger logger = LoggerFactory.getLogger(KettleConfig.class);

	@Autowired
	private KettleRepoProperties repoProperties;

	@Autowired
	private KettleRecordProperties recordProperties;

	@Bean
	public Repository kettleRepo() throws KettleException {
		logger.info("Kettle的客户端正在初始化....");
		KettleEnvironment.init();
		KettleFileRepository repository = new KettleFileRepository();
		RepositoryMeta repositoryMeta = new KettleFileRepositoryMeta(repoProperties.getId(), repoProperties.getName(),
				repoProperties.getDescription(), repoProperties.getBaseDirectory());
		repository.init(repositoryMeta);
		repository.connect(repoProperties.getUser(), repoProperties.getPasswd());
		StringBuffer slaves = new StringBuffer("");
		for (SlaveServer slaveServer : repository.getSlaveServers()) {
			if (slaves.length() > 0) {
				slaves.append(",");
			}
			slaves.append(slaveServer.getName());
		}
		logger.info("Kettle初始化发现注册的服务端[" + slaves + "]!");
		return repository;
	}

	/**
	 * Kettle的资源库
	 * 
	 * @return
	 * @throws KettleException
	 */
	@Bean
	public KettleRepoRepository kettleRepoRepository(Repository repository) throws KettleException {
		logger.info("Kettle的资源库在初始化....");
		return new KettleRepoRepository(repoProperties, repository);
	}
	
	@Bean
	public KettleRemotePool kettleRemotePool(KettleRepoRepository kettleRepoRepository) throws KettleException {
		logger.info("Kettle的远程节点池在初始化....");
		return new KettleRemotePool(kettleRepoRepository);
	}
}
