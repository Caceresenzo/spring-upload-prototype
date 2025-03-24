package com.example.demo.web.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SubmissionDto {

	private long id;
	private UserDto user;
	private String message;
	private LocalDateTime createdAt;
	private List<SubmissionFileDto> files;

}