package com.example.demo.storage;

import java.util.Objects;

public record SimpleObjectIdentifier(
	String key,
	String versionId
) implements ObjectIdentifier {

	public SimpleObjectIdentifier {
		Objects.requireNonNull(key, "key");
	}

}