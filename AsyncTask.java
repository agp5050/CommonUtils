package com.agp.ai.thread;

import com.agp.ai.bean.EmailConfig;
import com.agp.ai.service.AssemblySendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class AsyncTask {
    private final Logger logger = LoggerFactory.getLogger(AsyncTask.class);

    @Autowired
    private AssemblySendService assemblySendService;

    @Async
    public void queryAndSendEmail(EmailConfig emailConfig, String upTable, String columnName, boolean isUpdateDb) {
        long currentTimeMillis = System.currentTimeMillis();
        assemblySendService.queryAndSendEmail(emailConfig, upTable, columnName, isUpdateDb);
        logger.info("==========AsyncTask.queryAndSendEmail group["+emailConfig.getGroup()+"],time consuming [" + (System.currentTimeMillis() - currentTimeMillis) + "ms]");
    }

    @Async
    public void queryAndSendToAM(EmailConfig emailConfig) {
        long currentTimeMillis = System.currentTimeMillis();
        assemblySendService.queryAndSendToAM(emailConfig);
        logger.info("==========AsyncTask.queryAndSendToAM group["+emailConfig.getGroup()+"],time consuming [" + (System.currentTimeMillis() - currentTimeMillis) + "ms]");
    }

    @Async
    public void assemblyFeatureScore(EmailConfig emailConfig) {
        long currentTimeMillis = System.currentTimeMillis();
        assemblySendService.assemblyFeatureScore(emailConfig);
        logger.info("==========AsyncTask.assemblyFeatureScore group["+emailConfig.getGroup()+"],time consuming [" + (System.currentTimeMillis() - currentTimeMillis) + "ms]");
    }

    @Async
    public void generateAfReport(EmailConfig emailConfig) {
        long currentTimeMillis = System.currentTimeMillis();
        assemblySendService.generateAfReport(emailConfig);
        logger.info("==========AsyncTask.generateAfReport group["+emailConfig.getGroup()+"],time consuming [" + (System.currentTimeMillis() - currentTimeMillis) + "ms]");
    }
}
