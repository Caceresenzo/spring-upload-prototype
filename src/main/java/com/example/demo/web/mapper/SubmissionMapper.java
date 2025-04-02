package com.example.demo.web.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.domain.submission.Submission;
import com.example.demo.domain.submission.SubmissionFile;
import com.example.demo.web.dto.SubmissionDto;
import com.example.demo.web.dto.SubmissionFileDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SubmissionMapper {

	private final UserMapper userMapper;

	public SubmissionDto toDto(Submission submission) {
		return new SubmissionDto()
			.setId(submission.getId())
			.setUser(userMapper.toDto(submission.getUser()))
			.setMessage(submission.getMessage())
			.setCreatedAt(submission.getCreatedAt())
			.setFiles(
				submission.getFiles()
					.stream()
					.map(this::toDto)
					.toList()
			);
	}

	public SubmissionFileDto toDto(SubmissionFile submissionFile) {
		return new SubmissionFileDto()
			.setId(submissionFile.getId())
			.setName(submissionFile.getName())
			.setSize(submissionFile.getSize())
			.setMediaType(submissionFile.getMediaType());
	}

}