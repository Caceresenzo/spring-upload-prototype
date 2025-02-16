package com.example.demo.domain.upload;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "uploads")
@Data
@Accessors(chain = true)
@FieldNameConstants
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Upload {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NaturalId
	@Column(nullable = false)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	private UUID uuid;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private long size;

	@Column
	private Long chunkCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Column
	private String statusMessage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;

	@Column(nullable = false)
	private String providerId;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@CreationTimestamp
	@Column(nullable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = UploadChunk.Fields.upload, cascade = CascadeType.PERSIST)
	private List<UploadChunk> chunks;

	public Upload(
		String name,
		long size,
		Long chunkSize,
		Provider provider,
		Duration expiration
	) {
		this.uuid = UUID.randomUUID();

		this.name = name;
		this.size = size;

		this.status = Status.PENDING;

		this.provider = provider;

		this.createdAt = LocalDateTime.now();
		this.expiresAt = createdAt.plus(expiration);

		if (chunkSize != null) {
			this.chunkCount = 0l;
			this.generateChunks(chunkSize);
		} else {
			this.chunkCount = null;
		}
	}

	private void generateChunks(long maxChunkSize) {
		this.chunks = new ArrayList<>();

		var offset = 0l;
		var remaining = size;

		while (remaining > maxChunkSize) {
			final var chunkSize = maxChunkSize;

			addChunk(
				offset,
				chunkSize,
				false
			);

			remaining -= chunkSize;
			offset += chunkSize;
		}

		if (remaining != 0) {
			final var chunkSize = remaining;

			addChunk(
				offset,
				chunkSize,
				true
			);

			offset += chunkSize;
		}
	}

	private void addChunk(long offset, long size, boolean last) {
		++chunkCount;

		this.chunks.add(new UploadChunk(
			this,
			chunkCount,
			offset,
			size,
			last
		));
	}

	public boolean isChunked() {
		return chunkCount != null;
	}

	public List<String> getChunkHashes() {
		return chunks.stream()
			.map(UploadChunk::getHash)
			.toList();
	}

	public boolean hasIncompleteChunk() {
		if (!isChunked()) {
			throw new IllegalStateException("non chunked upload cannot be incomplete");
		}

		return chunks.stream()
			.anyMatch(UploadChunk::isIncompleted);
	}

	public void setStatus(Status status, String message) {
		this.status = status;
		this.statusMessage = message;
	}

	public enum Status {

		PENDING,
		IN_PROGRESS,
		SUCCEEDED,
		FAILED,

	}

	public enum Provider {

		AWS_S3,

	}

}