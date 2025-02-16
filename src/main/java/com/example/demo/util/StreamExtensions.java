package com.example.demo.util;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamExtensions {

	public static <T, R> Stream<R> mapWithIndex(Stream<T> stream, BiFunction<? super T, Long, ? extends R> mapper) {
		final var index = new long[] { 0 };

		return stream.map((item) -> mapper.apply(item, index[0]++));
	}

}