package com.cryptoclyx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CryptoClyxServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoClyxServerApplication.class, args);
	}

}
