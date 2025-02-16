package com.example.demo.storage;

public interface ObjectIdentifier {

	String key();

	String versionId();

	public static ObjectIdentifier of(String key) {
		return new SimpleObjectIdentifier(key, null);
	}

	public static ObjectIdentifier of(String key, String versionId) {
		return new SimpleObjectIdentifier(key, versionId);
	}

}