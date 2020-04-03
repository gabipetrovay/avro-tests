package com.example.avro;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.PropertyReader;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TestKafkaAvroSslConsumer extends PropertyReader {

    private static Consumer<String, String> consumer;

    @BeforeAll
    public static void initializeConsumer() throws UnknownHostException {

        Properties config = new Properties();

        config.put(ConsumerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG,
                getProperty("kafka.consumer.group-prefix", "") + "test-gabriel-group-" + UUID.randomUUID());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getProperty("kakfa.bootstrap.servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getCanonicalName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getCanonicalName());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, getProperty("kafka.schema-registry.url"));

        if (getProperty("kafka.ssl.truststore.location") != null) {
            // Kakfa consumer SSL configuration
            config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                    resolveRelativeResourcesPath(getProperty("kafka.ssl.truststore.location")));
            config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, getProperty("kafka.ssl.truststore.password"));
            config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                    resolveRelativeResourcesPath(getProperty("kafka.ssl.keystore.location")));
            config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, getProperty("kafka.ssl.keystore.password"));
            config.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, getProperty("kafka.ssl.key.password"));

            // Kakfa Registry client SSL configuration
            config.put(SchemaRegistryClientConfig.CLIENT_NAMESPACE + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                    config.getProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
            config.put(SchemaRegistryClientConfig.CLIENT_NAMESPACE + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                    config.getProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
            config.put(SchemaRegistryClientConfig.CLIENT_NAMESPACE + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                    config.getProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
            config.put(SchemaRegistryClientConfig.CLIENT_NAMESPACE + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
                    config.getProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
        }

        consumer = new KafkaConsumer<String, String>(config);

        String topic = getProperty("kafka.topics.avro");
        consumer.subscribe(Arrays.asList(topic));
    }

    @AfterAll
    public static void closeConsumer() throws UnknownHostException {

        consumer.close();
    }

    @Test
    public void testConsumeMessageSync() throws InterruptedException, ExecutionException {

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));

        System.out.println("Record count: " + records.count());

        assertTrue(records.count() > 0);

        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }

}
