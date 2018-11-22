

import com.alibaba.fastjson.JSON;
import com.jffox.cloud.config.Configs;
import com.jffox.cloud.entity.Offsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class KafkaGroupConsumeLagChecker {
    Configs configs;
    public KafkaGroupConsumeLagChecker(Configs configs){
        this.configs=configs;
    }

    public List<Offsets> checkConsumerLag() {
        List<Offsets> rstOffsets=new ArrayList<>();
        Map<String, List<String>> groupFilter = configs.getGroupFilter();
        for (Map.Entry<String,List<String>> groupTopics:groupFilter.entrySet()) {
            String group = groupTopics.getKey();
            List<String> topics = groupTopics.getValue();
            //显式关闭consumer since 1.7 jdk
            try (KafkaConsumer<String, String> consumer =
                         new KafkaConsumer<>(getConsumerProperties(configs.getBootstrapServers(), group))) {
                for (String topic : topics) {
                    List<TopicPartition> topicPartitions = new ArrayList<>();
                    List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
                    for (PartitionInfo info : partitionInfos) {
                        topicPartitions.add(new TopicPartition(info.topic(), info.partition()));
                    }
//                    consumer.assign(topicPartitions);
                    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
                    for (TopicPartition partition : topicPartitions) {
                        long lag=0;
                        OffsetAndMetadata offsetAndMetadata = consumer.committed(partition);
                        if (offsetAndMetadata != null) {
                            Long endOffset = endOffsets.get(partition);
                            if (endOffset != null) {
                                lag= (endOffset - offsetAndMetadata.offset());
                                Offsets offsetsEntity = new Offsets();
                                offsetsEntity.setGroup(group);
                                offsetsEntity.setTopic(topic);
                                offsetsEntity.setPartition(partition.partition());
                                offsetsEntity.setOffset(offsetAndMetadata.offset());
                                offsetsEntity.setLogSize(endOffset);
                                offsetsEntity.setLag(lag);
                                offsetsEntity.setOwner("NA");
                                Date date = new Date();
                                offsetsEntity.setCreation(date);
                                offsetsEntity.setModified(date);
                                rstOffsets.add(offsetsEntity);
                            }
                        }

                    }

                }
            }
        }
        log.info(JSON.toJSONString(rstOffsets));
        return rstOffsets;
    }
    private static Properties getConsumerProperties(String bootstrap, String group){
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    StringDeserializer.class.getName());
            return props;
        }
}
