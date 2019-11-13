

import com.te.datalake.util.DateUtils;
import com.te.datalake.util.ShutdownCallback;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import static com.te.datalake.constant.Constants.DOUBLE_AND;

@Slf4j
@Data
public class NewConsumerThread<K, V> extends Thread implements Closeable {
    public interface ConsumerCallBack<T> {
        void consume(T t);
    }

    private Consumer<K, V> consumer;
    private long totalConsumedMsg = 0L;
    private List<String> topics;
    private ConsumerCallBack consumerCallBack;
    private boolean flag = true;
    private Queue<String> msgQueue;
    private long sleepTime = 300L;
    private boolean backPressureSwitch=true;

    @Override
    public void close()  {
        flag = false;
        //This method is thread-safe and is useful in particular to abort a long poll.
        try{
            consumer.wakeup();
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
        while (!msgQueue.isEmpty()) {
            log.info(DateUtils.getCurrentTime() + " 当前Thread name：" + Thread.currentThread().getName() + " msgQueue is not Empty waiting for msgQueue consume, remaining numbers {}", msgQueue.size());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try{
            consumer.commitSync();
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
        try {
            consumer.close();
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
        log.info("closed current consumer thread {}", this.getName());
    }

    public NewConsumerThread(Consumer<K, V> consumer, List<String> topiclist, Queue<String> msgQueue, ConsumerCallBack consumerCallBack) {
        this.consumer = consumer;
        this.topics = topiclist;
        this.msgQueue = msgQueue;
        this.consumerCallBack = consumerCallBack;
        ShutdownCallback.register(this);
        consumer.subscribe(topics);
    }

    public NewConsumerThread(Consumer<K, V> consumer,
                             List<String> topicList,
                             Queue<String> msgQueue,
                             ConsumerCallBack consumerCallBack,
                             Boolean fromBeginning) {
        this.consumer = consumer;
        topics = topicList;
        this.msgQueue = msgQueue;
        this.consumerCallBack = consumerCallBack;
        ShutdownCallback.register(this);
        if (fromBeginning) {
            consumer.subscribe(topics, new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    consumer.seekToBeginning(partitions);
                }
            });
        } else {
            consumer.subscribe(topics);
        }
        log.info("consumer thread {} started", this.getName());

    }


    public void run() {
        while (flag) {
            checkMsgQueue(msgQueue);
            if (backPressureSwitch){
                pullOnce();
            }
            try {
                //太快消费不完OutOfMemoryError: Java heap space
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!flag){
                //Commit offsets returned on the last {@link #poll(long) poll()} for all the subscribed list of topics and partitions
            consumer.commitSync();
            }
        }
    }

    private void pullOnce() {
        ConsumerRecords<K, V> poll;
        try{
            poll = consumer.poll(200);
        }catch (Exception e){
            return;
        }
        for (ConsumerRecord record : poll) {
            if (consumerCallBack != null) {
                consumerCallBack.consume(record.value());
                totalConsumedMsg++;
            } else {
                msgQueue.add(record.value().toString());
            }
        }
    }

    private void checkMsgQueue(Queue<String> msgQueue) {
        long l = System.currentTimeMillis();
        int size = msgQueue.size();
        if ( size< 200) {
            //直接==0，提交的太频繁，导致日志疯狂的涨几个小时好几G
            if (msgQueue.size() == 0 && l % 100 == 0) {
                this.consumer.commitSync();
            }
            minusInterval();
            if (backPressureSwitch==false){
                backPressureSwitch=true;
            }
//            if (l % 3 == 0) {
//                System.out.println(DateUtils.getCurrentTime() + " 当前Thread name：" + Thread.currentThread().getName() + " 正进行minusInterval  ... msgQueue size is:" + msgQueue.size() + "  interval is " + sleepTime + " milliseconds");
//            }
        } else if (size > 4000 && size<40000) {
            increaseInterval();
            if (l % 3 == 0) {
                System.out.println(DateUtils.getCurrentTime() + " 当前Thread name：" + Thread.currentThread().getName() + " 正进行increaseInterval ... msgQueue size is:" + msgQueue.size() + "  interval is " + sleepTime + " milliseconds");
            }
        }else if (size > 40000){
            backPressureSwitch=false;
        }
    }

    //最大睡眠8s
    private void increaseInterval() {
        long tmp = (long) (sleepTime * 1.4);
        sleepTime = tmp > 8000 ? 8000 : tmp;
    }

    private void minusInterval() {
        long tmp = (long) (sleepTime / 1.3);
        sleepTime = tmp < 20 ? 50 : tmp;
    }
}



