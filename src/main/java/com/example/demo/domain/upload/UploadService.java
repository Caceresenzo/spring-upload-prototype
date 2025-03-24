package com.example.demo.domain.upload;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import com.example.demo.configuration.properties.UploadProperties;
import com.example.demo.domain.upload.Upload.Provider;
import com.example.demo.domain.upload.Upload.Status;
import com.example.demo.domain.upload.exception.IncompleteUploadException;
import com.example.demo.domain.upload.exception.InvalidUploadChunkHashException;
import com.example.demo.domain.upload.exception.MissingUploadChunkHashException;
import com.example.demo.domain.upload.exception.UploadChunkNotFoundException;
import com.example.demo.domain.upload.exception.UploadNotFoundException;
import com.example.demo.storage.BlobStore;
import com.example.demo.storage.ObjectIdentifier;
import com.example.demo.storage.PresignedUploadRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

	private final UploadRepository uploadRepository;
	private final UploadChunkRepository uploadChunkRepository;
	private final UploadProperties uploadProperties;
	private final BlobStore blobStore;

	@Transactional(propagation = Propagation.REQUIRED)
	public Upload getUpload(UUID id) {
		return uploadRepository.findByUuid(id)
			.orElseThrow(() -> new UploadNotFoundException(id));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public UploadChunk getChunk(Upload upload, long number) {
		return uploadChunkRepository.findByUploadAndNumber(upload, number)
			.orElseThrow(() -> new UploadChunkNotFoundException(upload, number));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public Upload create(
		String name,
		DataSize size,
		Optional<DataSize> preferredChunkSize
	) {
		Long chunkSize = uploadProperties.getChunkSize().clamp(preferredChunkSize).toBytes();
		final var expiration = uploadProperties.getMaxDuration();

		if (blobStore.getMinimumChunkUploadSize().compareTo(size) > 0) {
			chunkSize = null;
		}

		var upload = new Upload(
			name,
			size.toBytes(),
			chunkSize,
			Provider.AWS_S3,
			expiration
		);

		return uploadRepository.save(upload);
	}

	public PresignedUploadRequest getChunkRequest(UploadChunk chunk) {
		final var upload = chunk.getUpload();

		if (upload.isChunked()) {
			upload.setStatus(Status.IN_PROGRESS, UploadStatusMessages.inProgress(chunk));

			if (!upload.hasProviderId()) {
				final var providerId = blobStore.beginChunkedUpload(
					toStorageKey(upload),
					getUploadMetadata(upload)
				);

				upload.setProviderId(providerId);
			}

			uploadRepository.save(upload);

			return blobStore.presignChunkUploadRequest(
				toStorageKey(upload),
				upload.getProviderId(),
				uploadProperties.getMaxDuration(),
				(int) chunk.getNumber(),
				DataSize.ofBytes(chunk.getSize())
			);
		} else {
			upload.setStatus(Status.IN_PROGRESS, UploadStatusMessages.inProgress());

			return blobStore.presignDirectUploadRequest(
				toStorageKey(upload),
				uploadProperties.getMaxDuration(),
				DataSize.ofBytes(chunk.getSize()),
				getUploadMetadata(upload)
			);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public UploadChunk confirmChunk(UploadChunk chunk, String hash) {
		final var upload = chunk.getUpload();

		BlobStore.HashValidationResult result;
		if (upload.isChunked()) {
			result = blobStore.validateChunkHash(
				toStorageKey(upload),
				upload.getProviderId(),
				(int) chunk.getNumber(),
				hash
			);
		} else {
			result = blobStore.validateDirectHash(
				toStorageKey(upload),
				hash
			);
		}

		switch (result) {
			case MISSING -> throw new MissingUploadChunkHashException(chunk);
			case MISMATCH -> throw new InvalidUploadChunkHashException(chunk);
			case VALID -> {}
		}

		chunk.confirm(hash);

		return uploadChunkRepository.save(chunk);
	}

	public Upload abort(Upload upload, String reason) {
		log.info("aborting upload - id={} reason=`{}`", upload.getId(), reason);

		upload.setStatus(Upload.Status.FAILED, reason);

		if (upload.isChunked() && upload.hasProviderId()) {
			final var aborted = blobStore.abortChunkedUpload(
				toStorageKey(upload),
				upload.getProviderId()
			);

			if (!aborted) {
				log.warn("could not abort chunked upload, has it already been aborted? - providerId={}", upload.getProviderId());
			}
		}

		return uploadRepository.save(upload);
	}

	public Upload complete(Upload upload) {
		if (upload.hasIncompleteChunk()) {
			abort(upload, UploadStatusMessages.incomplete());

			throw new IncompleteUploadException(upload);
		}

		if (upload.isChunked()) {
			blobStore.completeChunkedUpload(
				toStorageKey(upload),
				upload.getProviderId(),
				upload.getChunkHashes()
			);
		}

		upload.setStatus(Upload.Status.SUCCEEDED, UploadStatusMessages.done());
		return uploadRepository.save(upload);
	}

	public ObjectIdentifier toStorageKey(Upload upload) {
		return ObjectIdentifier.of("uploads/%s".formatted(upload.getUuid()));
	}

	private Map<String, String> getUploadMetadata(Upload upload) {
		return Map.of(
			"user.id", "1",
			"upload.id", String.valueOf(upload.getId()),
			"upload.uuid", String.valueOf(upload.getUuid())
		);
	}

}