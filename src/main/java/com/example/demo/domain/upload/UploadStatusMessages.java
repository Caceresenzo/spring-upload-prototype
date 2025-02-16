package com.example.demo.domain.upload;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UploadStatusMessages {

	private static final String INCOMPLETE = "The upload is incomplete. Some chunk are missing.";
	private static final String IN_PROGRESS = "The upload is in progress.";
	private static final String IN_PROGRESS_CHUNK = "The upload is in progress. Uploading chunk #%d.";
	private static final String DONE = "The upload is done.";
	private static final String EXPIRED = "The upload has expired.";

	public static String incomplete() {
		return INCOMPLETE;
	}

	public static String inProgress() {
		return IN_PROGRESS.formatted();
	}

	public static String inProgress(UploadChunk chunk) {
		return IN_PROGRESS_CHUNK.formatted(chunk.getNumber());
	}

	public static String done() {
		return DONE;
	}

	public static String expired() {
		return EXPIRED;
	}

}