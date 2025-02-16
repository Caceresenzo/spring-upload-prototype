package com.example.demo.domain.upload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UploadCleaner {

	private final UploadRepository uploadRepository;
	private final UploadService uploadService;

	@Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void run() {
		final var pageable = PageRequest.of(0, 10);
		final var now = LocalDateTime.now();
		final var statuses = List.of(Upload.Status.PENDING, Upload.Status.IN_PROGRESS);

		Page<Upload> uploads;
		do {
			uploads = uploadRepository.findAllByStatusInAndExpiresAtLessThan(statuses, now, pageable);

			for (final var upload : uploads) {
				uploadService.abort(upload, UploadStatusMessages.expired());
			}
		} while (!uploads.isEmpty());
	}

}