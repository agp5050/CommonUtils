
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class TaskManager {
    private static final long Interval=24*60*60*1000;

    public static void startDailyTask(TimerTask task,int hour,int minute){
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        Date date = calendar.getTime();
        //如果设定的时间比这个方法执行的时间早，就把设定的时间+1
        if (date.before(new Date())){
            calendar.add(Calendar.DATE,1);
        }
        Timer timer=new Timer();
        timer.schedule(task,calendar.getTime(),Interval);
    }

}
