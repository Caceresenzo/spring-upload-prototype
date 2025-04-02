package com.example.demo.domain.submission;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.upload.Upload;
import com.example.demo.domain.upload.UploadService;
import com.example.demo.domain.upload.exception.UnusableUploadException;
import com.example.demo.domain.user.User;
import com.example.demo.storage.ObjectIdentifier;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

	private final SubmissionRepository submissionRepository;

	private final UploadService uploadService;

	@SneakyThrows
	@Transactional(propagation = Propagation.REQUIRED)
	public Submission create(
		User user,
		String message,
		Map<String, UUID> uploadIdByFilePaths
	) {
		final var submission = submissionRepository.save(new Submission(
			user,
			message
		));

		log.info("creating submission - submission.id={} uploadIdByFilePaths.size()={}", submission.getId(), uploadIdByFilePaths.size());

		final var uploads = uploadIdByFilePaths.entrySet()
			.stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				this::getUpload
			));

		/* copy to final location */
		for (final var entry : uploads.entrySet()) {
			final var name = entry.getKey();
			final var upload = entry.getValue();

			log.info("moving file - submission.id={} name=`{}` upload.uuid={} upload.size={}", submission.getId(), name, upload.getUuid(), upload.getSize());

			uploadService.consume(
				upload,
				toStorageKey(submission.getId(), name)
			);

			submission.addFile(name, upload.getSize(), upload.getMediaType());
		}

		log.info("saving - submission.id={}", submission.getId());

		return submissionRepository.save(submission);
	}

	private Upload getUpload(Entry<String, UUID> entry) {
		final var upload = uploadService.getUpload(entry.getValue());

		if (!upload.isSuccessful()) {
			throw new UnusableUploadException(upload);
		}

		return upload;
	}

	public ObjectIdentifier toStorageKey(long submissionId, String name) {
		return ObjectIdentifier.of("submissions/%s/%s".formatted(submissionId, name));
	}

}