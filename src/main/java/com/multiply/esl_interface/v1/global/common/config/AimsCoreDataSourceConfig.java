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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.multiply.esl_interface.v1.web.mapper.aimscore", sqlSessionFactoryRef = "aimscoreSqlSessionFactory", sqlSessionTemplateRef = "aimscoreSqlSessionTemplate")
@EnableTransactionManagement
public class AimsCoreDataSourceConfig {

    @Bean(name = "aimscoreDataSource")
    @ConfigurationProperties(prefix = "spring.aims-core-datasource.hikari")
    public DataSource aimscoreDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "aimscoreSqlSessionFactory")
    public SqlSessionFactory aimscoreSqlSessionFactory(@Qualifier("aimscoreDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.multiply.esl_interface.v1.web.mapper.aimscore");
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/com/multiply/esl_interface/v1/web/mapper/aimscore/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "aimscoreSqlSessionTemplate")
    public SqlSessionTemplate aimscoreSqlSessionTemplate(@Qualifier("aimscoreSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "aimscoreTransactionManager")
    public PlatformTransactionManager aimscoreTransactionManager(@Qualifier("aimscoreDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
