package com.example.demo.domain.upload;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "upload_chunks")
@Data
@Accessors(chain = true)
@FieldNameConstants
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class UploadChunk {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	private Upload upload;

	@Column(nullable = false)
	private long number;

	@Column(nullable = false)
	private long offset;

	@Column(nullable = false)
	private long size;

	@Column(nullable = false)
	private boolean last;

	@Column(nullable = false)
	private boolean completed;

	@Column
	private LocalDateTime completedAt;

	@Column
	private String hash;

	public UploadChunk(Upload upload, long number, long offset, long size, boolean last) {
		this.upload = upload;
		this.number = number;
		this.offset = offset;
		this.size = size;
		this.last = last;
	}

	public boolean isIncompleted() {
		return !completed;
	}

	public void confirm(String hash) {
		this.hash = hash;
		this.completed = true;
		this.completedAt = LocalDateTime.now();
	}

}