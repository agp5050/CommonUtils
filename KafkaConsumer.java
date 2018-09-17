import com.jfbank.ai.consumer.util.ShutdownCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;



/**
 * @param <K> message key type
 * @param <V> message value type
 */
@Slf4j
public class KafkaCusumerThread<K, V> extends Thread implements Closeable {
    private final KafkaConsumer<K, V> consumer;
    private List<String> topics;
    private ConsumerCallback<ConsumerRecord<K, V>> consumerCallback;




    public KafkaCusumerThread(KafkaConsumer<K, V> consumer, List<String> topics, ConsumerCallback<ConsumerRecord<K, V>> consumerCallback) {
        ShutdownCallback.register(this);
        this.consumer = consumer;
        this.topics = topics;
        this.consumerCallback = consumerCallback;
    }

    private boolean flag = true;

    private ConsumerRecords<K, V> records;

    @Override
    public void run() {
        consumer.subscribe(topics);
        while (flag) {
            try {
                records = consumer.poll(200);
                for (ConsumerRecord<K, V> record : records) {
                    consumerCallback.consumer(record);
                }
            } catch (Exception e) {
                records = null;
            }
            records = null;
        }
    }

    @Override
    public void close() throws IOException {
        log.info("start close kafka consumer.");
        consumer.commitSync();
        consumer.close();
        flag = false;
        for (int i = 0; i < 10; i++) {
            try {
                if (records != null && !records.isEmpty()) {
                    log.info("kafka consumer is closing -> {}", i);
                    Thread.sleep(200);
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                log.error(e.toString());
            }
        }
        log.info("finished closed kafka consumer.");

    }

    public interface ConsumerCallback<C> {
        void consumer(C t);
    }

}


