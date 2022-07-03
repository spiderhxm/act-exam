package org.activiti.examples.service;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author ：xm.hu
 * @description：TODO
 * @date ：2021/12/12 4:54 下午
 */
@Service(value="processText")
public class ProcessText implements JavaDelegate , InitializingBean {
    private Logger logger = LoggerFactory.getLogger(ProcessText.class);
    @Override
    public void execute(DelegateExecution execution) {
        final Map variables = execution.getVariables();
        String contentToProcess = (String) variables.get("content");

        if (contentToProcess.contains("activiti")) {
            logger.info("> Approving content: " + contentToProcess);
            execution.setVariable("approved",
                    true);
        } else {
            logger.info("> Discarding content: " + contentToProcess);
            execution.setVariable("approved",
                    false);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("ProcessText initialized!");
    }
}
