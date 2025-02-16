package com.example.demo.storage.aws;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.unit.DataSize;

import com.example.demo.storage.BlobStore;
import com.example.demo.storage.ObjectIdentifier;
import com.example.demo.storage.ObjectInfo;
import com.example.demo.storage.PresignedUploadRequest;
import com.example.demo.util.StreamExtensions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchUploadException;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@RequiredArgsConstructor
@ExtensionMethod(StreamExtensions.class)
public class S3BlobStore implements BlobStore {

	public static final DataSize MAXIMUM_DIRECT_UPLOAD_SIZE = DataSize.ofGigabytes(5);
	public static final DataSize MINIMUM_CHUNK_UPLOAD_SIZE = DataSize.ofMegabytes(5);

	private final @NonNull S3Client s3Client;
	private final @NonNull S3Utilities s3Utilities;
	private final @NonNull S3Presigner s3Presigner;
	private final @NonNull String bucketName;

	@Override
	public DataSize getMaximumDirectUploadSize() {
		return MAXIMUM_DIRECT_UPLOAD_SIZE;
	}

	@Override
	public DataSize getMinimumChunkUploadSize() {
		return MINIMUM_CHUNK_UPLOAD_SIZE;
	}

	/**
	 * Setting an expiration on multipart upload is not directly supported, a bucket policy must be created: https://docs.aws.amazon.com/AmazonS3/latest/userguide/mpu-abort-incomplete-mpu-lifecycle-config.html
	 */
	@Override
	public String beginChunkedUpload(ObjectIdentifier identifier, Map<String, String> metadata) {
		final var response = s3Client.createMultipartUpload(
			CreateMultipartUploadRequest.builder()
				.bucket(bucketName)
				.key(identifier.key())
				.metadata(metadata)
				.build()
		);

		return response.uploadId();
	}

	@SneakyThrows
	@Override
	public PresignedUploadRequest presignChunkUploadUri(ObjectIdentifier identifier, String uploadId, Duration expiration, int chunkNumber, DataSize size) {
		final var response = s3Presigner.presignUploadPart(
			UploadPartPresignRequest.builder()
				.uploadPartRequest(
					UploadPartRequest.builder()
						.bucket(bucketName)
						.key(identifier.key())
						.uploadId(uploadId)
						.partNumber(chunkNumber)
						.contentLength(size.toBytes())
						.build()
				)
				.signatureDuration(expiration)
				.build()
		);

		return new PresignedUploadRequest(
			mapMethod(response.httpRequest().method()),
			response.url().toURI(),
			response.signedHeaders()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, (entry) -> entry.getValue().getFirst()))
		);
	}

	@Override
	public ChunkHashValidationResult validateChunkHash(ObjectIdentifier identifier, String uploadId, int chunkNumber, String hash) {
		final var response = s3Client.listParts(
			ListPartsRequest.builder()
				.bucket(bucketName)
				.key(identifier.key())
				.uploadId(uploadId)
				.partNumberMarker(chunkNumber - 1)
				.maxParts(1)
				.build()
		);

		final var part = response.parts()
			.stream()
			.filter((part_) -> part_.partNumber() == chunkNumber)
			.findFirst()
			.orElse(null);

		if (part == null) {
			return ChunkHashValidationResult.MISSING;
		}

		if (!part.eTag().equals(hash)) {
			return ChunkHashValidationResult.MISMATCH;
		}

		return ChunkHashValidationResult.VALID;
	}

	@Override
	public ObjectInfo completeChunkedUpload(ObjectIdentifier identifier, String uploadId, List<String> hashes) {
		final var parts = hashes.stream()
			.mapWithIndex((hash, index) -> CompletedPart.builder()
				.partNumber((int) (index + 1))
				.eTag(hash)
				.build()
			)
			.toList();

		final var response = s3Client.completeMultipartUpload(
			CompleteMultipartUploadRequest.builder()
				.bucket(bucketName)
				.key(identifier.key())
				.uploadId(uploadId)
				.multipartUpload(
					CompletedMultipartUpload.builder()
						.parts(parts)
						.build()
				)
				.build()
		);

		return new ObjectInfo(response.key(), response.versionId(), response.eTag(), -1);
	}

	@Override
	public boolean abortChunkedUpload(ObjectIdentifier identifier, String uploadId) {
		try {
			s3Client.abortMultipartUpload(
				AbortMultipartUploadRequest.builder()
					.bucket(bucketName)
					.key(identifier.key())
					.uploadId(uploadId)
					.build()
			);

			return true;
		} catch (NoSuchUploadException __) {
			return false;
		}
	}

	public static PresignedUploadRequest.HttpMethod mapMethod(SdkHttpMethod sdkHttpMethod) {
		return switch (sdkHttpMethod) {
			case POST -> PresignedUploadRequest.HttpMethod.POST;
			case PUT -> PresignedUploadRequest.HttpMethod.PUT;
			default -> throw new IllegalStateException("unsupport for upload method: %s".formatted(sdkHttpMethod));
		};
	}

}