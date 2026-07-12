package com.themoa.policysearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ThemoaPolicySearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThemoaPolicySearchApplication.class, args);
	}

}
