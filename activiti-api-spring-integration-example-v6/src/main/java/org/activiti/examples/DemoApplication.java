package org.activiti.examples;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.*;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import javax.annotation.Resource;

@SpringBootApplication
@EnableIntegration
public class DemoApplication implements InitializingBean
        //implements CommandLineRunner
        {
    @Resource(name = "repositoryService")
    private RepositoryService repositoryService;
    @Resource(name = "runtimeService")
    private RuntimeService runtimeService;


    private String INPUT_DIR = "/tmp/";
    private String FILE_PATTERN = "*.txt";
    private Logger logger = LoggerFactory.getLogger(DemoApplication.class);


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

    }


    public void run(String... args) {

        Deployment deployment = this.repositoryService.createDeployment().addClasspathResource("processes/categorize-text.bpmn20.xml")
                .deploy();

        logger.info("> Deployment: " + deployment.getName());

    }

    @Bean({ "processEngine" })
    public ProcessEngine getProcessEngine(){
        ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                //.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
                .setJdbcUrl("jdbc:h2:mem:my-own-db;DB_CLOSE_DELAY=1000")
                .setAsyncExecutorActivate(false)
                .buildProcessEngine();
        return processEngine;
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

    @Bean
    public MessageChannel fileChannel() {
        return new DirectChannel();
    }

    @Bean
    @InboundChannelAdapter(value = "fileChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource sourceReader = new FileReadingMessageSource();
        sourceReader.setDirectory(new File(INPUT_DIR));
        sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
        return sourceReader;
    }

    @ServiceActivator(inputChannel = "fileChannel")
    public void processFile(Message<File> message) throws IOException {
        File payload = message.getPayload();
        logger.info(">>> Processing file: " + payload.getName());

        String content = FileUtils.readFileToString(payload, "UTF-8");

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

        logger.info("> Processing content: " + content + " at " + formatter.format(new Date()));
        long start = System.currentTimeMillis();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("categorizeProcess");

        long costTime = System.currentTimeMillis() - start;
        logger.info("Activiti End, cost={} ", (Object) costTime);
        logger.info(">>> Created Process Instance: " + processInstance);

        logger.info(">>> Deleting processed file: " + payload.getName());
        payload.delete();

    }


            @Override
            public void afterPropertiesSet() throws Exception {
                this.run();
            }
        }
