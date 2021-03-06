import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExecutorTaskManager {
    private static final long Interval=24*60*60*1000;

    public static void startDailyTask(ScheduledExecutorService task,Runnable runnable, int hour, int minute){
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);

        //如果设定的时间比这个方法执行的时间早，就把设定的时间+1
        Date date = new Date();
        if (calendar.getTime().before(date)){
            calendar.add(Calendar.DATE,1);
        }
        long delay = calendar.getTime().getTime() - date.getTime();
        task.scheduleAtFixedRate(runnable,delay,Interval, TimeUnit.MILLISECONDS);
    }

}
