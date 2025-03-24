package com.example.demo.configuration;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TikaConfiguration {

	@Bean
	Tika tika() {
		return new Tika();
	}

}