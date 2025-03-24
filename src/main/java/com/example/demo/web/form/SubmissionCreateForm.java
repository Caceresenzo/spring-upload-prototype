package com.example.demo.web.form;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionCreateForm {

	private long userId;

	@NotNull
	private String message;

	@NotEmpty
	private Map</* @RelativePath */ String, UUID> files;

}