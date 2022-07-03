package org.activiti.examples.cnfig;

import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration("bgActivitiConfig")
public class ActivitiConfig
{
    //是否保存历史日志
    @Value("${activiti.history}")
    private String actHistory;
    //执行方式
    @Value("${activiti.asyncExecutorActivate}")
    private Boolean actAsync;

    @Value("${activiti.tableprefix}")
    private String actTablePrefix;

    @Value("${activiti.schemaupdate}")
    private String schemaUpdate;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Resource(name = "flowIdGenerator")
    private FlowIdGenerator flowIdGenerator;
    
/*    @Bean({ "activitiTxManager" })
    public PlatformTransactionManager platformTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }*/

    @Resource(name = "actTxManager")
    private PlatformTransactionManager platformTransactionManager;
    
    @Bean({ "springProcessEngineConfiguration" })
    public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
        final SpringProcessEngineConfiguration springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
        springProcessEngineConfiguration.setDataSource(dataSource);
        springProcessEngineConfiguration.setTransactionManager(this.platformTransactionManager);
        //引擎启停时数据库的执行策略
        //请使用false为默认值，设置为该值后，Activiti在启动时，会对比数据库表中保存的版本，如果没有表或者版本不匹配时，将在启动时抛出异常。
        springProcessEngineConfiguration.setDatabaseSchemaUpdate(schemaUpdate);
        springProcessEngineConfiguration.setHistory(actHistory);
        springProcessEngineConfiguration.setAsyncExecutorActivate((boolean)actAsync);
        springProcessEngineConfiguration.setIdGenerator((IdGenerator)flowIdGenerator);
        springProcessEngineConfiguration.setDatabaseTablePrefix(actTablePrefix);
        return springProcessEngineConfiguration;
    }
    //流程引擎工厂类
    @Bean({ "processEngineFactoryBean" })
    public ProcessEngineFactoryBean processEngineFactoryBean() {
        final ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean.setProcessEngineConfiguration((ProcessEngineConfigurationImpl)this.springProcessEngineConfiguration());
        return processEngineFactoryBean;
    }
    
    public ProcessEngine getProcessEngine() {
        try {
            return this.processEngineFactoryBean().getObject();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Bean({ "repositoryService" })
    public RepositoryService repositoryService() {
        return getProcessEngine().getRepositoryService();
    }
    
    @Bean({ "runtimeService" })
    public RuntimeService runtimeService() {
        return getProcessEngine().getRuntimeService();
    }
    
    @Bean({ "taskService" })
    public TaskService taskService() {
        return getProcessEngine().getTaskService();
    }
    
    @Bean({ "historyService" })
    public HistoryService historyService() {
        return getProcessEngine().getHistoryService();
    }
    
    @Bean({ "identityService" })
    public IdentityService identityService() {
        return getProcessEngine().getIdentityService();
    }
}
