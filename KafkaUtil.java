import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import vip.itstar.VO.RuleInfo;
import java.util.Collections;
import java.util.Properties;
/**
 * KafkaUtil类
 */
public class KafkaUtil {
    // kafka topic名称
    private final String TOPIC = "test";
    // 声明变量producer
    private Producer<String, byte[]> producer;
    // 声明变量consumer
    private KafkaConsumer<String, byte[]> consumer;
    // KafkaUtil构造
    public KafkaUtil() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "node5:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Properties props1 = new Properties();
        props1.put("bootstrap.servers", "node5:9092");
        props1.put("group.id", "test");
        props1.put("enable.auto.commit", "true");
        props1.put("auto.commit.interval.ms", "1000");
        props1.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props1.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        producer = new KafkaProducer<>(props);
        consumer = new KafkaConsumer<>(props1);
    }
    // Kafka 发送数据
    public void send(String json) {
        producer.send(new ProducerRecord(TOPIC, json));
    }
    // Kafka 发送数据(指定topic类型)
    public void send(String json, String topic) {
        producer.send(new ProducerRecord(topic, json));
    }

    public static void main(String[] args) {
        KafkaUtil kafkaUtil = new KafkaUtil();
        kafkaUtil.poll();
    }
    // 接受数据
    private void poll() {
        consumer.subscribe(Collections.singletonList(TOPIC));
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(100);
            for (ConsumerRecord<String, byte[]> record : records) {
                RuleInfo ruleInfo = (RuleInfo) KryoUtil.out(record.value(), new RuleInfo());
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), ruleInfo.getIsCashonDelivery());
            }
        }
    }
    // 自定义topic接受数据
    private void poll(String topic) {
        consumer.subscribe(Collections.singletonList(topic));
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(100);
            for (ConsumerRecord<String, byte[]> record : records) {
                RuleInfo ruleInfo = (RuleInfo) KryoUtil.out(record.value(), new RuleInfo());
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), ruleInfo.getIsCashonDelivery());
            }
        }
    }
    // 生产者关闭
    public void close() {
        producer.close();
    }
}
