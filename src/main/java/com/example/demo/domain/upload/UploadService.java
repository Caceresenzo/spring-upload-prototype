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
		final var chunkSize = uploadProperties.getChunkSize().clamp(preferredChunkSize).toBytes();
		final var expiration = uploadProperties.getMaxDuration();

		if (blobStore.getMinimumChunkUploadSize().compareTo(size) > 0) {
			throw new UnsupportedOperationException("direct upload not supported yet");
		}

		final var upload = new Upload(
			name,
			size.toBytes(),
			chunkSize,
			Provider.AWS_S3,
			expiration
		);

		final var metadata = Map.of(
			"user.id", "1",
			"upload.uuid", String.valueOf(upload.getUuid())
		);

		final var providerId = blobStore.beginChunkedUpload(
			toStorageKey(upload),
			metadata
		);

		upload.setProviderId(providerId);

		return uploadRepository.save(upload);
	}

	public PresignedUploadRequest getChunkUri(UploadChunk chunk) {
		final var upload = chunk.getUpload();

		upload.setStatus(Status.IN_PROGRESS, UploadStatusMessages.inProgress(chunk));
		uploadRepository.save(upload);

		return blobStore.presignChunkUploadUri(
			toStorageKey(upload),
			upload.getProviderId(),
			uploadProperties.getMaxDuration(),
			(int) chunk.getNumber(),
			DataSize.ofBytes(chunk.getSize())
		);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public UploadChunk confirmChunk(UploadChunk chunk, String hash) {
		final var upload = chunk.getUpload();

		final var result = blobStore.validateChunkHash(
			toStorageKey(upload),
			upload.getProviderId(),
			(int) chunk.getNumber(),
			hash
		);

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

		final var aborted = blobStore.abortChunkedUpload(
			toStorageKey(upload),
			upload.getProviderId()
		);

		if (!aborted) {
			log.warn("could not abort chunked upload, has it already been aborted? - providerId={}", upload.getProviderId());
		}

		return uploadRepository.save(upload);
	}

	public Upload complete(Upload upload) {
		if (upload.hasIncompleteChunk()) {
			abort(upload, UploadStatusMessages.incomplete());

			throw new IncompleteUploadException(upload);
		}

		blobStore.completeChunkedUpload(
			toStorageKey(upload),
			upload.getProviderId(),
			upload.getChunkHashes()
		);

		upload.setStatus(Upload.Status.SUCCEEDED, "done");
		return uploadRepository.save(upload);
	}

	public ObjectIdentifier toStorageKey(Upload upload) {
		return ObjectIdentifier.of("uploads/%s".formatted(upload.getUuid()));
	}

}