package com.example.demo.web.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SubmissionFileDto {

	private long id;
	private String name;
	private long size;
	private String mediaType;

}