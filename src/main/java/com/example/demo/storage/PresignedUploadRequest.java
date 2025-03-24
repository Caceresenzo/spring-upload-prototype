package com.example.demo.storage;

import java.net.URI;
import java.util.Map;

public record PresignedUploadRequest(
	HttpMethod method,
	URI url,
	Map<String, String> headers
) {

	public enum HttpMethod {

		POST,
		PUT,

	}

}