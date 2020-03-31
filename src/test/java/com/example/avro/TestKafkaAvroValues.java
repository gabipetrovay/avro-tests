package com.example.avro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.PropertyReader;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
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
public class TestKafkaAvroValues extends PropertyReader {

    private static final String STATIC_KEY = "static-key";

    private static Producer<String, String> producer;
    private static Consumer<String, String> consumer;

    @BeforeAll
    public static void initializeProducer() throws UnknownHostException {

        Properties config = new Properties();

        config.put(ProducerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getCanonicalName());
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getProperty("kakfa.bootstrap.servers"));
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, getProperty("kafka.schema-registry.url"));

        producer = new KafkaProducer<String, String>(config);
    }

    @BeforeAll
    public static void initializeConsumer() throws UnknownHostException {

        Properties config = new Properties();

        config.put(ConsumerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "string-group");
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getProperty("kakfa.bootstrap.servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getCanonicalName());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, getProperty("kafka.schema-registry.url"));

        consumer = new KafkaConsumer<String, String>(config);

        String topic = getProperty("kafka.topics.avro");
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

        String topic = getProperty("kafka.topics.avro");

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
