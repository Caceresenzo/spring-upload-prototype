package com.example.demo.storage;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.util.unit.DataSize;

public interface BlobStore {

	DataSize getMaximumDirectUploadSize();

	DataSize getMinimumChunkUploadSize();

	PresignedUploadRequest presignDirectUploadRequest(ObjectIdentifier identifier, Duration expiration, DataSize size, Map<String, String> metadata);

	HashValidationResult validateDirectHash(ObjectIdentifier storageKey, String hash);

	String beginChunkedUpload(ObjectIdentifier identifier, Map<String, String> metadata);

	PresignedUploadRequest presignChunkUploadRequest(ObjectIdentifier identifier, String uploadId, Duration expiration, int chunkNumber, DataSize size);

	HashValidationResult validateChunkHash(ObjectIdentifier identifier, String uploadId, int chunkNumber, String hash);

	ObjectInfo completeChunkedUpload(ObjectIdentifier identifier, String uploadId, List<String> hashes);

	boolean abortChunkedUpload(ObjectIdentifier identifier, String uploadId);

	enum HashValidationResult {

		MISSING,
		MISMATCH,
		VALID;

	}

}