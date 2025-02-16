package com.example.demo.web.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UploadCreateForm {

	@NotNull
	// @RelativePath
	private String name;

	@NotNull
	@PositiveOrZero
	private Long size;

	@Positive
	private Long preferredChunkSize;

}