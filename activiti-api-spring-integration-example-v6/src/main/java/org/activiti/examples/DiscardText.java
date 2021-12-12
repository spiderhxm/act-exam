package org.activiti.examples;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author ：xm.hu
 * @description：TODO
 * @date ：2021/12/12 4:54 下午
 */
@Service(value="discardText")
public class DiscardText implements JavaDelegate {
    private Logger logger = LoggerFactory.getLogger(DiscardText.class);
    @Override
    public void execute(DelegateExecution execution) {
        final Map variables = execution.getVariables();
        String contentToDiscard = (String)  variables.get("content");

        contentToDiscard += " :( ";
        execution.setVariable("content",
                contentToDiscard);
        logger.info("Final Content: " + contentToDiscard);
    }
}
