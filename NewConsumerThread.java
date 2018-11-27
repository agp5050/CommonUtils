import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

public class NewConsumerThread<K,V> extends Thread implements Closeable {
    private static final Logger log = LoggerFactory
            .getLogger(NewConsumerThread.class);
    private  Consumer<K,V> consumer;
    private List<String> topics;
    private ConsumerCallBack consumerCallBack;
    private static boolean flag=true;
    private boolean needPause=false;
    private Queue<String> msgQueue=null;
    private long sleepTime=300L;
    public void setNeedPause(boolean needPause){
        this.needPause=needPause;
    }
    public void setFlag(boolean flag){
        this.flag=flag;
    }
    public static boolean getFlag(){
        return flag;
    }
    @Override
    public void close() throws IOException {
        flag=false;
        while (!msgQueue.isEmpty()){
            log.info("msgQueue is not Empty waiting for msgQueue consume, remaining numbers {}",msgQueue.size());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        consumer.commitSync();
        consumer.close();
    }
    public NewConsumerThread(Consumer<K,V> consumer, List<String> topiclist,Queue<String> msgQueue){
        this.consumer=consumer;
        topics=topiclist;
        this.msgQueue=msgQueue;
        ShutdownCallback.register(this);

    }
    public NewConsumerThread(Consumer<K,V> consumer, List<String> topiclist,ConsumerCallBack consumerCallBack){
        this.consumer=consumer;
        topics=topiclist;
        this.consumerCallBack=consumerCallBack;
        ShutdownCallback.register(this);

    }
    public void run(){
        consumer.subscribe(topics);
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
            if (needPause){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                //太快消费不完OutOfMemoryError: Java heap space
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            consumer.commitSync();
        }
    }

    private void checkMsgQueue(Queue<String> msgQueue) {
        if(msgQueue.size()<2000){
            minusInterval();
            System.out.println("minusInterval  ... msgQueue size is:"+msgQueue.size()+"  interval is "+sleepTime+" milliseconds");
        }else if(msgQueue.size()>5000){
            increaseInterval();
            System.out.println("increaseInterval ... msgQueue size is:"+msgQueue.size()+"  interval is "+sleepTime+" milliseconds");
        }
    }
    //最大睡眠5s
    private void increaseInterval() {
        long tmp=(long)(sleepTime*1.2);
        sleepTime = tmp>5000 ? 5000: tmp;
    }

    private void minusInterval() {
        sleepTime=(long)(sleepTime/1.2);
    }
}

interface ConsumerCallBack<T>{
    void consume(T t);
}

