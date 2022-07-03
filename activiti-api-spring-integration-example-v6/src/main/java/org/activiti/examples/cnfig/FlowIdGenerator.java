package org.activiti.examples.cnfig;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("flowIdGenerator")
public class FlowIdGenerator implements IdGenerator
{
    @Resource(name = "snowflake")
    private Snowflake snowflake;

    @Bean({ "snowflake" })
    public Snowflake snowflake() {
        return new Snowflake(1, 999);
    }


    public String getNextId() {
        return Long.toString(this.snowflake.nextId());
    }
}
