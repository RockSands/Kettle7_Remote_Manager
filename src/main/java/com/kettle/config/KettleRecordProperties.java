package com.kettle.config;

import org.springframework.beans.factory.annotation.Value;

//@Component
//@PropertySource("classpath:/config/kettle_env.properties")
public class KettleRecordProperties {

    @Value("kettle.record.maxPreRemote")
    private int maxPreRemote;

    @Value("kettle.record.runningTimeOut")
    private int runningTimeOut;
    
    @Value("kettle.record.pool.max")
    private int poolMax;

    public int getMaxPreRemote() {
        return maxPreRemote;
    }

    public void setMaxPreRemote(int maxPreRemote) {
        this.maxPreRemote = maxPreRemote;
    }

    public int getRunningTimeOut() {
        return runningTimeOut;
    }

    public void setRunningTimeOut(int runningTimeOut) {
        this.runningTimeOut = runningTimeOut;
    }

    public int getPoolMax() {
        return poolMax;
    }

    public void setPoolMax(int poolMax) {
        this.poolMax = poolMax;
    }
}
