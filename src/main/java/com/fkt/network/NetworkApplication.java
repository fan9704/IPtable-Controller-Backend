package com.fkt.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NetworkApplication {
	private static final Logger log = LoggerFactory.getLogger(NetworkApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(NetworkApplication.class, args);
		log.info("Web Document Reference http://127.0.0.1:9990/swagger-ui/index.html");
		log.info("Server is Running on http://127.0.0.1:9990");
	}

}
