package com.example.demo.domain.upload.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.demo.domain.upload.Upload;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class IncompleteUploadException extends RuntimeException {

	private final UUID uploadId;

	public IncompleteUploadException(Upload upload) {
		super("upload is incomplete");

		this.uploadId = upload.getUuid();
	}

}