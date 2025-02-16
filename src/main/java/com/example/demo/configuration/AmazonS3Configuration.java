package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.configuration.properties.AmazonProperties;
import com.example.demo.storage.aws.S3BlobStore;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration(proxyBeanMethods = false)
public class AmazonS3Configuration {

	@Bean
	Region region(AmazonProperties properties) {
		return properties.getRegion();
	}

	@Bean
	AwsCredentials awsCredentials(AmazonProperties properties) {
		final var credentials = properties.getCredentials();
		return AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey());
	}

	@Bean
	AwsCredentialsProvider awsCredentialsProvider(AwsCredentials awsCredentials) {
		return StaticCredentialsProvider.create(awsCredentials);
	}

	@Bean
	S3Client s3Client(AwsCredentialsProvider awsCredentialsProvider, Region region) {
		return S3Client
			.builder()
			.credentialsProvider(awsCredentialsProvider)
			.region(region)
			.build();
	}

	@Bean
	S3Utilities s3Utilities(S3Client s3Client) {
		return s3Client.utilities();
	}

	@Bean
	S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider, Region region) {
		return S3Presigner.builder()
			.credentialsProvider(credentialsProvider)
			.region(region)
			.build();
	}

	@Bean
	S3BlobStore s3BlobStore(
		S3Client s3Client,
		S3Utilities s3Utilities,
		S3Presigner s3Presigner,
		AmazonProperties properties
	) {
		return new S3BlobStore(s3Client, s3Utilities, s3Presigner, properties.getBucketName());
	}

}