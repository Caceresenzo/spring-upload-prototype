package com.example.demo.web.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.domain.upload.Upload;
import com.example.demo.domain.upload.UploadChunk;
import com.example.demo.web.dto.UploadChunkDto;
import com.example.demo.web.dto.UploadDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UploadMapper {

	public UploadDto toDto(Upload upload) {
		return new UploadDto()
			.setId(upload.getUuid())
			.setName(upload.getName())
			.setSize(upload.getSize())
			.setChunked(upload.isChunked())
			.setStatus(upload.getStatus())
			.setStatusMessage(upload.getStatusMessage())
			.setProvider(upload.getProvider())
			.setExpiresAt(upload.getExpiresAt())
			.setCreatedAt(upload.getCreatedAt());
	}

	public UploadDto toDtoWithChunks(Upload upload) {
		return toDto(upload)
			.setChunks(
				upload.getChunks()
					.stream()
					.map(this::toDto)
					.toList()
			);
	}

	public UploadChunkDto toDto(UploadChunk chunk) {
		return new UploadChunkDto()
			.setNumber(chunk.getNumber())
			.setOffset(chunk.getOffset())
			.setSize(chunk.getSize())
			.setLast(chunk.isLast())
			.setCompleted(chunk.isCompleted())
			.setCompletedAt(chunk.getCompletedAt());
	}

}