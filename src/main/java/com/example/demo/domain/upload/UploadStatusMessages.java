package com.example.demo.domain.upload;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UploadStatusMessages {

	private static final String INCOMPLETE = "The upload is incomplete. Some chunk are missing.";
	private static final String IN_PROGRESS_UPLOAD_CHUNK_X = "The upload is in progress. Uploading chunk #%d.";
	private static final String EXPIRED = "The upload has expired.";

	public static String incomplete() {
		return INCOMPLETE;
	}

	public static String inProgress(UploadChunk chunk) {
		return IN_PROGRESS_UPLOAD_CHUNK_X.formatted(chunk.getNumber());
	}

	public static String expired() {
		return EXPIRED;
	}

}