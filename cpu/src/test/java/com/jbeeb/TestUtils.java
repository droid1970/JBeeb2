package com.jbeeb;

import java.io.InputStream;

public final class TestUtils {
    public static InputStream stream(final String resourceName) {
        return TestUtils.class.getResourceAsStream(resourceName);
    }
}
