package com.qtra.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QtraScannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(QtraScannerApplication.class, args);
	}

}
