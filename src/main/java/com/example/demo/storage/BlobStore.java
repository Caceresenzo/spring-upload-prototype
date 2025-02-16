package com.example.demo.storage;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.util.unit.DataSize;

public interface BlobStore {

	DataSize getMaximumDirectUploadSize();

	DataSize getMinimumChunkUploadSize();

	String beginChunkedUpload(ObjectIdentifier identifier, Map<String, String> metadata);

	PresignedUploadRequest presignChunkUploadUri(ObjectIdentifier identifier, String uploadId, Duration expiration, int chunkNumber, DataSize size);

	ChunkHashValidationResult validateChunkHash(ObjectIdentifier identifier, String uploadId, int chunkNumber, String hash);

	ObjectInfo completeChunkedUpload(ObjectIdentifier identifier, String uploadId, List<String> hashes);

	void abortChunkedUpload(ObjectIdentifier identifier, String uploadId);

	enum ChunkHashValidationResult {

		MISSING,
		MISMATCH,
		VALID;

	}

}