

import com.te.datalake.util.DateUtils;
import com.te.datalake.util.ShutdownCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
@Slf4j
public class NewConsumerThread<K,V> extends Thread implements Closeable{
    private  Consumer<K,V> consumer;
    private List<String> topics;
    private ConsumerCallBack consumerCallBack;
    private static boolean flag=true;
    private Queue<String> msgQueue=null;
    private long sleepTime=300L;
    @Override
    public void close() throws IOException {
        flag=false;
        while (!msgQueue.isEmpty()){
            log.info(DateUtils.getCurrentTime()+" 当前Thread name："+Thread.currentThread().getName()+" msgQueue is not Empty waiting for msgQueue consume, remaining numbers {}",msgQueue.size());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        consumer.commitSync();
        consumer.close();
    }
    public NewConsumerThread(Consumer<K,V> consumer, List<String> topiclist,Queue<String> msgQueue,ConsumerCallBack consumerCallBack){
        this.consumer=consumer;
        topics=topiclist;
        this.msgQueue=msgQueue;
        this.consumerCallBack=consumerCallBack;
        ShutdownCallback.register(this);
        consumer.subscribe(topics);
    }

    public NewConsumerThread(Consumer<K,V> consumer,
                             List<String> topiclist,
                             Queue<String> msgQueue,
                             ConsumerCallBack consumerCallBack,
                             Boolean fromBeginning){
        this.consumer=consumer;
        topics=topiclist;
        this.msgQueue=msgQueue;
        this.consumerCallBack=consumerCallBack;
        ShutdownCallback.register(this);
        if (fromBeginning){
            consumer.subscribe(topics,new ConsumerRebalanceListener(){
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    consumer.seekToBeginning(partitions);
                }
            });
        }else {
            consumer.subscribe(topics);
        }

    }


    public void run(){
        while (flag){
            ConsumerRecords<K, V> poll = consumer.poll(200);
            checkMsgQueue(msgQueue);
            for (ConsumerRecord record:poll){
                if (msgQueue==null){
                    consumerCallBack.consume(record);
                }else {
                    msgQueue.add((String) record.value());
                }
            }

            try {
                //太快消费不完OutOfMemoryError: Java heap space
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            consumer.commitSync();
        }
    }

    private void checkMsgQueue(Queue<String> msgQueue) {
        long l = System.currentTimeMillis();
        if(msgQueue.size()<1000){
            //直接==0，提交的太频繁，导致日志疯狂的涨几个小时好几G
            if (msgQueue.size()==0 && l%1000==0){
                this.consumer.commitSync();
            }
            minusInterval();
            if (l%3==0){
                System.out.println(DateUtils.getCurrentTime()+" 当前Thread name："+Thread.currentThread().getName()+" 正进行minusInterval  ... msgQueue size is:"+msgQueue.size()+"  interval is "+sleepTime+" milliseconds");
            }
        }else if(msgQueue.size()>4000){
            increaseInterval();
            if (l%3==0){
                System.out.println(DateUtils.getCurrentTime()+" 当前Thread name："+Thread.currentThread().getName()+" 正进行increaseInterval ... msgQueue size is:"+msgQueue.size()+"  interval is "+sleepTime+" milliseconds");
            }
        }
    }
    //最大睡眠8s
    private void increaseInterval() {
        long tmp=(long)(sleepTime*1.4);
        sleepTime = tmp>8000 ? 8000: tmp;
    }

    private void minusInterval() {
        long tmp=(long)(sleepTime/1.3);
        sleepTime =  tmp<20 ? 50 : tmp;
    }
}

interface ConsumerCallBack<T>{
    void consume(T t);
}

