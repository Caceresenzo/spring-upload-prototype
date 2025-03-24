package com.example.demo.web.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.domain.user.User;
import com.example.demo.web.dto.UserDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserMapper {

	public UserDto toDto(User user) {
		return new UserDto()
			.setId(user.getId())
			.setLogin(user.getLogin())
			.setCreatedAt(user.getCreatedAt());
	}

}