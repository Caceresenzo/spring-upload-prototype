package com.example.demo.web.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadChunkCompleteForm {

	@NotNull
	private String hash;

}