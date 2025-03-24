package com.example.demo.web.rest;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.submission.SubmissionService;
import com.example.demo.domain.user.exception.UserService;
import com.example.demo.web.dto.SubmissionDto;
import com.example.demo.web.form.SubmissionCreateForm;
import com.example.demo.web.mapper.SubmissionMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/v1/submissions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubmissionRestControllerV1 {

	private final SubmissionService submissionService;
	private final UserService userService;
	private final SubmissionMapper submissionMapper;

	@PostMapping
	public SubmissionDto createSubmission(
		@RequestBody @Validated SubmissionCreateForm form
	) {
		final var user = userService.get(form.getUserId());

		final var submission = submissionService.create(
			user,
			form.getMessage(),
			form.getFiles()
		);

		return submissionMapper.toDto(submission);
	}

}