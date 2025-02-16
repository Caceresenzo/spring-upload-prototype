package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.SneakyThrows;

@SpringBootApplication
public class DemoApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}