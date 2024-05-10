package com.fkt.network;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = {NetworkApplication.class}) // Load Context
@ActiveProfiles({"test"}) // Use test profile
@ContextConfiguration(classes = BaseTestConfig.class)
@EnableMongoRepositories(basePackages = "com.fkt.network.repositories")
class NetworkApplicationTests {
	@Test
	void contextLoads() {

	}

}
