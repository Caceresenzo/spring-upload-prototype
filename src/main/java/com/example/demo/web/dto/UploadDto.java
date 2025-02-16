package com.example.demo.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.demo.domain.upload.Upload;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadDto {

	private UUID id;
	private String name;
	private long size;
	private boolean chunked;
	private Upload.Status status;
	private String statusMessage;
	private Upload.Provider provider;
	private LocalDateTime expiresAt;
	private LocalDateTime createdAt;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<UploadChunkDto> chunks;

}