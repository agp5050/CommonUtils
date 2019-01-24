
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * 异步线程池配置,主要用于异步spring项目的异步task。
 */
@Component
public class AsyncConfigurer implements org.springframework.scheduling.annotation.AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfigurer.class);

    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(5);//核心线程数
        threadPool.setMaxPoolSize(20);// 最大线程数
        threadPool.setQueueCapacity(10);//线程池所使用的缓冲队列
        threadPool.setWaitForTasksToCompleteOnShutdown(true);//等待任务在关机时完成--表明等待所有线程执行完
        threadPool.setAwaitTerminationSeconds(60 * 15);// 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        threadPool.setThreadNamePrefix("flow-calc-thread-");//  线程名称前缀
        threadPool.initialize(); // 初始化
        logger.info("--------------------------》》》开启异步线程池");
        return threadPool;
    }

    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {

        return new MyAsyncExceptionHandler();
    }

    /**
     * 自定义异常处理类
     *
     * @author flynn
     */
    class MyAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        //手动处理捕获的异常
        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
            logger.info("-------------》》》捕获线程异常信息");
            logger.info("Exception message - " + throwable.getMessage());
            logger.info("Method name - " + method.getName());
            for (Object param : obj) {
                logger.info("Parameter value - " + param);
            }
        }

    }
}
