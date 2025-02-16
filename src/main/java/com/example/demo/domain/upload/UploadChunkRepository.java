package com.example.demo.domain.upload;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface UploadChunkRepository extends JpaRepository<UploadChunk, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<UploadChunk> findByUploadAndNumber(Upload upload, long number);

}