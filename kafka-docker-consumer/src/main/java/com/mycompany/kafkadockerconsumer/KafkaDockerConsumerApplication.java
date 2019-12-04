package com.mycompany.kafkadockerconsumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalTime;
import java.util.*;

@SpringBootApplication
public class KafkaDockerConsumerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(KafkaDockerConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) {

        String bootstrap_server;
        List<String> topics;
        String group_id;

        if (args.length < 3) {
            System.err.println("java -jar target/kafka-docker-consumer <bootstrap_server> <group_id> <topics>");
            System.err.println("Example: java -jar target/kafka-docker-consumer localhost:29092 my-group BTC,LTC");
            System.exit(1);
        }

        bootstrap_server = args[0];
        group_id = args[1];
        topics = Arrays.asList(args[2].split(","));

        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrap_server);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("group.id", group_id);

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(topics);
        try {
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                for (ConsumerRecord record : records) {
                    System.out.println(String.format("[%s] Topic - %s, Partition - %d, Value: %s", LocalTime.now().toString(), record.topic(),
                        record.partition(), getRecordCryptoCurrentValue(record)));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            kafkaConsumer.close();
        }
    }

    private String getRecordCryptoCurrentValue(ConsumerRecord record) {
        String info = (String) record.value();
        String value = info.substring(info.indexOf("\"last\""), info.indexOf(", \"times")).replace("\"", "").trim();
        return value.substring(value.indexOf(":") + 1).trim();
    }
}