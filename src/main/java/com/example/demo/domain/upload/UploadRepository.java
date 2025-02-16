package com.example.demo.domain.upload;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface UploadRepository extends JpaRepository<Upload, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Upload> findByUuid(UUID uuid);

}