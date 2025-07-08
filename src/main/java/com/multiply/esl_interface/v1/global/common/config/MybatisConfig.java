package com.multiply.esl_interface.v1.global.common.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.multiply.esl_interface.v1.web.mapper.oracle", sqlSessionFactoryRef = "oracleSqlSessionFactory", sqlSessionTemplateRef = "oracleSessionTemplate")   // mapper 인터페이스의 경로와 동일해야 한다.
public class MybatisConfig {


	@Bean(name = "oracleDataSource")
	@ConfigurationProperties("spring.datasource.hikari")
	@Primary
	public DataSource oracleDataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

	@Bean(name = "oracleSqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(@Qualifier("oracleDataSource") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		return sqlSessionFactoryBean.getObject();
	}

	@Bean(name = "oracleSessionTemplate")
	public SqlSessionTemplate sqlSessionTemplate( @Qualifier("oracleSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}