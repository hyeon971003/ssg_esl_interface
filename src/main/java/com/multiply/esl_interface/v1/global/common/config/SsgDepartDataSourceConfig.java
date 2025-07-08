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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.multiply.esl_interface.v1.web.mapper.ssgdepart", sqlSessionFactoryRef = "ssgdepartSqlSessionFactory", sqlSessionTemplateRef = "ssgdepartSqlSessionTemplate")
@EnableTransactionManagement
public class SsgDepartDataSourceConfig {

    @Bean(name = "ssgdepartDataSource")
    @ConfigurationProperties(prefix = "spring.ssg-depart-datasource.hikari")
    public DataSource ssgdepartDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "ssgdepartSqlSessionFactory")
    public SqlSessionFactory ssgdepartSqlSessionFactory(@Qualifier("ssgdepartDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.multiply.esl_interface.v1.web.mapper.ssgdepart");
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/com/multiply/esl_interface/v1/web/mapper/ssgdepart/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "ssgdepartSqlSessionTemplate")
    public SqlSessionTemplate ssgdepartSqlSessionTemplate(@Qualifier("ssgdepartSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "ssgdepartTransactionManager")
    @Primary
    public PlatformTransactionManager ssgdepartTransactionManager(@Qualifier("ssgdepartDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
