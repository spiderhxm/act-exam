package org.activiti.examples.cnfig;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration("dataSourceConfig")
public class DataSourceConfig
{
    @Bean(name = { "dataSource" }, initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.druid.primary")
    public DataSource dataSource() {
        final DruidDataSource dataSource = new DruidDataSource();
        return (DataSource)dataSource;
    }


    @Bean({ "actTxManager" })
    public PlatformTransactionManager actTxManager() {
        return (PlatformTransactionManager)new DataSourceTransactionManager(this.dataSource());
    }

/*    @Bean(name = { "jdbcTemplate" })
    public JdbcTemplate newJdbcTemp() {
        return new JdbcTemplate(this.dataSource());
    }*/
}
