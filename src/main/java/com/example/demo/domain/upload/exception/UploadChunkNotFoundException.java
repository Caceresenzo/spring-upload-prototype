package com.example.demo.domain.upload.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.demo.domain.upload.Upload;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UploadChunkNotFoundException extends RuntimeException {

	private final UUID uploadId;
	private final long chunkNumber;

	public UploadChunkNotFoundException(Upload upload, long chunkNumber) {
		super("upload chunk not found");

		this.uploadId = upload.getUuid();
		this.chunkNumber = chunkNumber;
	}

}