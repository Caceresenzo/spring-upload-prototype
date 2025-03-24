package com.example.demo.web.rest;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.upload.UploadService;
import com.example.demo.domain.upload.exception.IncompleteUploadException;
import com.example.demo.storage.PresignedUploadRequest;
import com.example.demo.web.dto.UploadChunkDto;
import com.example.demo.web.dto.UploadDto;
import com.example.demo.web.form.UploadChunkCompleteForm;
import com.example.demo.web.form.UploadCreateForm;
import com.example.demo.web.mapper.UploadMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/uploads", produces = MediaType.APPLICATION_JSON_VALUE)
public class UploadRestControllerV1 {

	private final UploadService uploadService;
	private final UploadMapper uploadMapper;

	@PostMapping
	public UploadDto createUpload(
		@RequestBody @Validated UploadCreateForm form
	) {
		final var upload = uploadService.create(
			form.getName(),
			DataSize.ofBytes(form.getSize()),
			Optional.ofNullable(form.getPreferredChunkSize()).map(DataSize::ofBytes)
		);

		return uploadMapper.toDtoWithChunks(upload);
	}

	@GetMapping("{uploadId}")
	public UploadDto showUpload(
		@PathVariable UUID uploadId
	) {
		final var upload = uploadService.getUpload(uploadId);

		return uploadMapper.toDtoWithChunks(upload);
	}

	@Transactional
	@GetMapping("{uploadId}/chunks/{chunkNumber}/request")
	public PresignedUploadRequest getUploadChunkUri(
		@PathVariable UUID uploadId,
		@PathVariable long chunkNumber
	) {
		final var upload = uploadService.getUpload(uploadId);
		final var chunk = uploadService.getChunk(upload, chunkNumber);

		return uploadService.getChunkRequest(chunk);
	}

	@Transactional
	@PostMapping("{uploadId}/chunks/{chunkNumber}/confirm")
	public UploadChunkDto confirmUploadChunk(
		@PathVariable UUID uploadId,
		@PathVariable long chunkNumber,
		@RequestBody @Validated UploadChunkCompleteForm form
	) {
		final var upload = uploadService.getUpload(uploadId);
		var chunk = uploadService.getChunk(upload, chunkNumber);

		chunk = uploadService.confirmChunk(chunk, form.getHash());

		return uploadMapper.toDto(chunk);
	}

	@Transactional(
		noRollbackFor = {
			IncompleteUploadException.class
		}
	)
	@PostMapping("{uploadId}/complete")
	public UploadDto completeUpload(
		@PathVariable UUID uploadId
	) {
		var upload = uploadService.getUpload(uploadId);

		upload = uploadService.complete(upload);

		return uploadMapper.toDto(upload);
	}

	@Transactional
	@PostMapping("{uploadId}/abort")
	public UploadDto abortUpload(
		@PathVariable UUID uploadId
	) {
		var upload = uploadService.getUpload(uploadId);

		upload = uploadService.abort(upload, "user termination");

		return uploadMapper.toDto(upload);
	}

}