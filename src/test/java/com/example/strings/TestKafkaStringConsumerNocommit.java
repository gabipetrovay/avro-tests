package com.example.strings;

import com.example.PropertyReader;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class TestKafkaStringConsumerNocommit extends PropertyReader {

    private static Consumer<String, String> consumer;


    @BeforeAll
    public static void initializeProducer() throws UnknownHostException {

        Properties config = new Properties();

        config.put("client.id", InetAddress.getLocalHost().getHostName());
        config.put("group.id", "string-group");
        config.put("bootstrap.servers", getProperty("kakfa.bootstrap.servers"));
        config.put("key.deserializer", StringDeserializer.class.getCanonicalName());
        config.put("value.deserializer", StringDeserializer.class.getCanonicalName());
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", false);

        consumer = new KafkaConsumer<String, String>(config);

        String topic = getProperty("kafka.topics.strings");
        consumer.subscribe(Arrays.asList(topic));
    }

    @AfterAll
    public static void closeConsumer() throws UnknownHostException {

        consumer.close();
    }

    @Test
    @Order(2)
    public void testConsumeMessageSync() throws InterruptedException, ExecutionException {

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

        System.out.println("Record count: " + records.count());
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }

        consumer.commitSync();
    }

}
