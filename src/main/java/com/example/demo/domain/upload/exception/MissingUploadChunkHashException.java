package com.example.demo.domain.upload.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.demo.domain.upload.UploadChunk;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MissingUploadChunkHashException extends RuntimeException {

	private final UUID uploadId;
	private final long chunkNumber;

	public MissingUploadChunkHashException(UploadChunk chunk) {
		super("missing upload chunk hash");

		this.uploadId = chunk.getUpload().getUuid();
		this.chunkNumber = chunk.getNumber();
	}

}