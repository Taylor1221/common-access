package com.taylor.common.access.builder;


import java.util.LinkedList;
import java.util.List;

public class KeyBuilder {

    private static final String DEFAULT_DELIMITER = ":";

    private final String delimiter;

    private final List<String> values = new LinkedList<>();

    public KeyBuilder(String delimiter) {
        this.delimiter = delimiter;
    }

    public KeyBuilder() {
        this(DEFAULT_DELIMITER);
    }

    public KeyBuilder add(String value) {
        if (value != null && !value.isEmpty()) {
            values.add(value);
        }
        return this;
    }

    public String build() {
        return String.join(delimiter, values);
    }

}
