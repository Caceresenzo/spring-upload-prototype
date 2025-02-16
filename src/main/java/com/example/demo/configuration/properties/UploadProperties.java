package com.example.demo.configuration.properties;

import java.time.Duration;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

	private ChunkSize chunkSize = new ChunkSize();
	private Duration maxDuration = Duration.ofHours(10);

	@Data
	public static class ChunkSize {

		private DataSize minimum = DataSize.ofMegabytes(10);
		private DataSize preferred = DataSize.ofMegabytes(100);
		private DataSize maximum = DataSize.ofMegabytes(500);

		public DataSize clamp(Optional<DataSize> clientPreferredSize) {
			if (clientPreferredSize.isEmpty()) {
				return preferred;
			}

			return DataSize.ofBytes(Math.clamp(
				clientPreferredSize.get().toBytes(),
				minimum.toBytes(),
				maximum.toBytes()
			));
		}

	}

}