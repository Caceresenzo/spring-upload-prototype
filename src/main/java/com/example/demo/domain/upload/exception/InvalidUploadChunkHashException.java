package com.example.demo.domain.upload.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.demo.domain.upload.UploadChunk;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidUploadChunkHashException extends RuntimeException {

	private final UUID uploadId;
	private final long chunkNumber;

	public InvalidUploadChunkHashException(UploadChunk chunk) {
		super("invalid upload chunk hash");

		this.uploadId = chunk.getUpload().getUuid();
		this.chunkNumber = chunk.getNumber();
	}

}