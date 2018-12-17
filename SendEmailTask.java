package com.agp.ai.task;

import com.agp.ai.bean.Constants;
import com.agp.ai.bean.EmailConfig;
import com.agp.ai.service.AssemblySendService;
import com.agp.ai.thread.AsyncTask;
import com.agp.ai.util.DateUtil;
import com.agp.ai.util.EmailConfigUtil;
import com.agp.ai.util.ResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 发送邮件定时任务

 */
@SuppressWarnings("AlibabaClassMustHaveAuthor")
@Component
public class SendEmailTask {
    private final Logger logger = LoggerFactory.getLogger(SendEmailTask.class);

    @Autowired
    private Environment env;

    @Autowired
    private AssemblySendService assemblySendService;

    @Autowired
    private AsyncTask asyncTask;

    @Value("${message.summary.subject}")
    private String summarySubject;

    /**
     * 每天上午8:30执行一次
     */
    @Scheduled(cron = "${cron.day830}")
    public void sendEmailToDP() {
        logger.info("==========SendEmailTask.sendEmailToDP start==============");
        List<EmailConfig> emailConfigs = getEmailConfigs();
        for (EmailConfig emailConfig : emailConfigs) {
            String group = emailConfig.getGroup();
            if ("083001".equals(group)) {
                String queryDate = DateUtil.getPreviousDate();
                String sql = emailConfig.getQuerySql();
                if (!isSend(emailConfig)) continue;
                String querySql = sql.replaceAll("%s", queryDate);
                emailConfig.setQuerySql(querySql);
                asyncTask.queryAndSendEmail(emailConfig, "<div>数据查询时间：" + DateUtil.getPreviousDate() + "</div>",
                        "<tr><th>表名</th><th>数量</th></tr>", false);
            }
        }
        logger.info("==========SendEmailTask.sendEmailToDP end==============");
    }

    /**
     * 算法组监控需求监控
     */
    @Scheduled(cron = "${cron.day845}")
    public void sendEmailToAM() {
        logger.info("==========SendEmailTask.sendEmailToAM start==============");
        List<EmailConfig> emailConfigs = getEmailConfigs();
        for (EmailConfig emailConfig : emailConfigs) {
            String group = emailConfig.getGroup();
            if (isSend(emailConfig) && StringUtils.isNotBlank(group) && group.startsWith("8450")) {
                switch (group) {
                    case "845003":
                        //分位数
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        asyncTask.assemblyFeatureScore(emailConfig);
                        break;
                    case "845004":
                        //反欺诈报告
                        asyncTask.generateAfReport(emailConfig);
                        break;
                    default:
                        asyncTask.queryAndSendToAM(emailConfig);
                }
            }
        }
        logger.info("==========SendEmailTask.sendEmailToAM end==============");
    }

    /**
     * 每天上午8:00执行一次
     */
    @Scheduled(cron = "${cron.day800} ")
    public void sendEmailEightHour() {
        //反欺诈发送汇总数据
        String title = "<div>查询时间：" + DateUtil.getCurrentTime() + "</div>";
        asyncTask.queryAndSendEmail(
                new EmailConfig(ResourceUtil.getString(Constants.MESSAGE_SUMMARY_SQl), env.getProperty(Constants.MESSAGE_SUMMARY_TO), env.getProperty(Constants.MESSAGE_SUMMARY_CC),
                        summarySubject, "9999"), title, "", false);

        //其他八点执行的sql
        List<EmailConfig> emailConfigs = getEmailConfigs();
        for (EmailConfig emailConfig : emailConfigs) {
            if (!isSend(emailConfig)) continue;
            String group = emailConfig.getGroup();
            if (group.startsWith("0800")) {
                Integer n = Integer.valueOf(group.substring(4, 6));
                boolean flag = true;
                if (n >= 50) {
                    flag = false;
                }
                asyncTask.queryAndSendEmail(emailConfig, title, "", flag);
            }
        }
    }

    /**
     * 每小时执行一次
     */
    @Scheduled(cron = "${cron.oneHour} ")
    public void sendEmailPerHour() {
        logger.info("==========SendEmailTask.sendEmailPerHour start==============");
        List<EmailConfig> emailConfigs = getEmailConfigs();
        String title = "<div>查询时间：" + DateUtil.getCurrentTime() + "</div>";
        for (EmailConfig emailConfig : emailConfigs) {
            String group = emailConfig.getGroup();
            if (!isSend(emailConfig) || StringUtils.isBlank(group) || !group.startsWith("9001")) continue;
            asyncTask.queryAndSendEmail(emailConfig, title, "", true);
        }
        logger.info("==========SendEmailTask.sendEmailPerHour end==============");
    }

    /**
     * 每天凌晨00:30 初始化当天数据
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void initMessageDb() {
        logger.info("==========SendEmailTask.initMessageDb start==============");
        try {
            assemblySendService.initMessageDb();
        } catch (Exception e) {
            logger.error("==========SendEmailTask.initMessageDb exception", e);
        }
        logger.info("==========SendEmailTask.initMessageDb end==============");
    }

    private List<EmailConfig> getEmailConfigs() {
        List<EmailConfig> emailConfigs = EmailConfigUtil.getEmailConfigs();
        if (emailConfigs.isEmpty()) {
            return null;
        } else {
            return emailConfigs;
        }
    }

    private boolean isSend(EmailConfig emailConfig) {
        if (emailConfig == null) return false;
        if (StringUtils.isBlank(emailConfig.getEmailTo()) || StringUtils.isBlank(emailConfig.getQuerySql())) {
            return false;
        }
        return true;
    }
}
