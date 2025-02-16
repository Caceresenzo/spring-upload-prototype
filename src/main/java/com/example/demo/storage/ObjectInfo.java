package com.example.demo.storage;

public record ObjectInfo(
	String key,
	String versionId,
	String hash,
	long size
) implements ObjectIdentifier {}