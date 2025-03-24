package com.example.demo.web.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDto {

	private long id;
	private String login;
	private LocalDateTime createdAt;

}