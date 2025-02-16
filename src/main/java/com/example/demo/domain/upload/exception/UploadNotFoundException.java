package com.example.demo.domain.upload.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UploadNotFoundException extends RuntimeException {

	private final UUID uploadId;

	public UploadNotFoundException(UUID uploadId) {
		super("upload not found");

		this.uploadId = uploadId;
	}

}