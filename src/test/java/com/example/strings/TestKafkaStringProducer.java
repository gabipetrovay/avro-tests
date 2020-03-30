package com.example.strings;

import com.example.PropertyReader;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestKafkaStringProducer extends PropertyReader {

    private static final String STATIC_KEY = "static-key";

    private static Producer<String, String> producer;

    @BeforeClass
    public static void initializeProducer() throws UnknownHostException {

        Properties config = new Properties();

        config.put("client.id", InetAddress.getLocalHost().getHostName());
        config.put("key.serializer", StringSerializer.class.getCanonicalName());
        config.put("value.serializer", StringSerializer.class.getCanonicalName());
        config.put("bootstrap.servers", getProperty("kakfa.bootstrap.servers"));
        config.put("acks", "all");

        producer = new KafkaProducer<String, String>(config);
    }

    @AfterClass
    public static void closeProducer() throws UnknownHostException {

        producer.close();
    }

    @After
    public void flushProducer() {

        producer.flush();

    }

    @Test
    public void testProduceMessageSync() throws InterruptedException, ExecutionException {

        String topic = getProperty("kafka.topics.strings");

        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, STATIC_KEY,
                "this is the string message at " + new Date());
        Future<RecordMetadata> future = producer.send(record);

        RecordMetadata metadata = future.get();
        System.out.println("Timestamp: " + new Date(metadata.timestamp()));
        System.out.println("Offset: " + metadata.offset());

    }

}
