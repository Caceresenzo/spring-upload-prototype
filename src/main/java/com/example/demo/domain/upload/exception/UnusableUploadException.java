package com.example.demo.domain.upload.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.demo.domain.upload.Upload;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorProperty;
import lombok.Getter;

@Getter
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnusableUploadException extends RuntimeException {

	@ResponseErrorProperty
	private final UUID uploadId;

	@ResponseErrorProperty
	private final Upload.Status uploadStatus;

	@ResponseErrorProperty
	private final String uploadStatusMessage;

	public UnusableUploadException(Upload upload) {
		super("upload is unusable");

		this.uploadId = upload.getUuid();
		this.uploadStatus = upload.getStatus();
		this.uploadStatusMessage = upload.getStatusMessage();
	}

}