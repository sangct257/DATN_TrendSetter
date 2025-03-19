package com.example.datn_trendsetter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatnTrendSetterApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnTrendSetterApplication.class, args);

	}

}
