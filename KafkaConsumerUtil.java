import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
public class KafkaConsumerUtil {
    private static List<String> topics;
    private static Properties properties;
    /**
     * @return
     * application.yml需要有这些配置
     * ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG  bootstrap.servers
     * ConsumerConfig.GROUP_ID_CONFIG   group.id
     * ENABLE_AUTO_COMMIT_CONFIG  enable.auto.commit
     * AUTO_COMMIT_INTERVAL_MS_CONFIG auto.commit.interval.ms
     * KEY_DESERIALIZER_CLASS_CONFIG key.deserializer
     * VALUE_DESERIALIZER_CLASS_CONFIG value.deserializer
     */
    public static KafkaConsumer getInstance(){
        properties=new Properties();
        InputStream resourceAsStream = Object.class.getResourceAsStream("application.yml");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String topicstr = properties.getProperty("consumer.topics");
        topics=Arrays.asList(topicstr.split(","));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(topics);
        return consumer;
    }

    public static KafkaConsumer getCustomInstance(Properties properties){
        KafkaConsumerUtil.properties=properties;
        String topicstr = properties.getProperty("consumer.topics");
        topics=Arrays.asList(topicstr.split(","));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(topics);
        return consumer;
    }


}
//<version>1.0.1-cdh6.0.0</version>
