package com.example.avro;

import com.example.PropertyReader;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class TestKafkaAvroConsumer extends PropertyReader {

    private static Consumer<String, String> consumer;

    @BeforeClass
    public static void initializeProducer() throws UnknownHostException {

        Properties config = new Properties();

        config.put(ConsumerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "string-group");
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getProperty("kakfa.bootstrap.servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getCanonicalName());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, getProperty("kafka.schema-registry.url"));

        consumer = new KafkaConsumer<String, String>(config);

        String topic = getProperty("kafka.topics.avro");
        consumer.subscribe(Arrays.asList(topic));
    }

    @AfterClass
    public static void closeConsumer() throws UnknownHostException {

        consumer.close();
    }

    @Test
    public void testConsumeMessageSync() throws InterruptedException, ExecutionException {

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

        System.out.println("Record count: " + records.count());
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }

}
