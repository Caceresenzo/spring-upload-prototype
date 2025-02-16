package com.example.demo.domain.upload;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.LockOptions;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface UploadRepository extends JpaRepository<Upload, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Upload> findByUuid(UUID uuid);

	@SuppressWarnings("deprecation")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value = "" + LockOptions.SKIP_LOCKED))
	Page<Upload> findAllByStatusInAndExpiresAtLessThan(Collection<Upload.Status> statuses, LocalDateTime now, Pageable pageable);

}