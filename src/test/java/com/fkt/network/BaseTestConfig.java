package com.fkt.network;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


@TestConfiguration
@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BaseTestConfig.class})
public class BaseTestConfig {
    static MongoDBContainer container = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void registerMongoDBProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.url", container::getReplicaSetUrl);
    }
    static {
        container.start();
        System.setProperty("spring.data.mongodb.uri",container.getReplicaSetUrl());
    }
}
