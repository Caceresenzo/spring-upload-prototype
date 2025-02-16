package com.example.demo.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.ToString;
import software.amazon.awssdk.regions.Region;

@Data
@Component
@ConfigurationProperties("amazon")
public class AmazonProperties {

	private Region region = Region.EU_WEST_1;
	private Credentials credentials = new Credentials();
	private String bucketName;

	@Data
	public static class Credentials {

		@ToString.Exclude
		private String accessKey;

		@ToString.Exclude
		private String secretKey;

	}

}