package com.example.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.PropertyReader;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@TestMethodOrder(OrderAnnotation.class)
public class TestKafkaStringValues extends PropertyReader {

    private static final String STATIC_KEY = "static-key";
    private static final String KAKFA_TOPIC_KEY = "kafka.topics.strings";

    private static Producer<String, String> producer;
    private static Consumer<String, String> consumer;

    @BeforeAll
    public static void initializeProducer() throws UnknownHostException {

        Properties config = new Properties();

        config.put("client.id", InetAddress.getLocalHost().getHostName());
        config.put("key.serializer", StringSerializer.class.getCanonicalName());
        config.put("value.serializer", StringSerializer.class.getCanonicalName());
        config.put("bootstrap.servers", getProperty("kakfa.bootstrap.servers"));
        config.put("acks", "all");
        config.put("max.block.ms", 1000);

        producer = new KafkaProducer<String, String>(config);
    }

    @BeforeAll
    public static void initializeConsumer() throws UnknownHostException {

        Properties config = new Properties();

        config.put("client.id", InetAddress.getLocalHost().getHostName());
        config.put("group.id", "string-group");
        config.put("bootstrap.servers", getProperty("kakfa.bootstrap.servers"));
        config.put("key.deserializer", StringDeserializer.class.getCanonicalName());
        config.put("value.deserializer", StringDeserializer.class.getCanonicalName());
        config.put("auto.offset.reset", "earliest");

        consumer = new KafkaConsumer<String, String>(config);

        String topic = getProperty(KAKFA_TOPIC_KEY);
        consumer.subscribe(Arrays.asList(topic));
    }

    @AfterAll
    public static void closeConsumer() throws UnknownHostException {

        consumer.close();
    }

    @AfterAll
    public static void closeProducer() throws UnknownHostException {

        producer.close();
    }

    @Test
    @Order(1)
    public void testProduceMessageSync() throws InterruptedException, ExecutionException {

        String topic = getProperty(KAKFA_TOPIC_KEY);

        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, STATIC_KEY,
                "this is the string message at " + new Date());
        Future<RecordMetadata> future = producer.send(record);
        producer.flush();

        RecordMetadata metadata = future.get();
        System.out.println("Timestamp: " + new Date(metadata.timestamp()));
        System.out.println("Offset: " + metadata.offset());

    }

    @Test
    @Order(2)
    public void testConsumeMessageSync() throws InterruptedException, ExecutionException {

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

        System.out.println("Record count: " + records.count());

        assertEquals(1, records.count());

        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }

}
