package com.example;

import org.junit.jupiter.api.Test;

public class TestKafkaStderr extends PropertyReader {

    @Test
    public void testStderr() {

        System.err.println("This is an error");
        System.out.println("This is an output");
    }

}
