package com.example.demo.web.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadChunkDto {

	private long number;
	private long offset;
	private long size;
	private boolean last;
	private boolean completed;
	private LocalDateTime completedAt;

}