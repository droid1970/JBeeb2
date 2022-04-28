package com.jbeeb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class SystemStatusImpl implements SystemStatus {

    private final Map<String, String> statusEntries = new ConcurrentHashMap<>();

    public void putString(final String key, final String value) {
        statusEntries.put(Objects.requireNonNull(key), value);
    }

    public void putBoolean(final String key, final boolean value) {
        putString(key, Boolean.toString(value));
    }

    public void putInt(final String key, final int value) {
        putString(key, Integer.toString(value));
    }

    public void putLong(final String key, final long value) {
        putString(key, Long.toString(value));
    }

    public void putDouble(final String key, final double value) {
        putString(key, Double.toString(value));
    }

    public String getString(final String key, final String defaultValue) {
        return statusEntries.getOrDefault(Objects.requireNonNull(key), defaultValue);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        final String b = getString(key, Boolean.toString(defaultValue));
        return "true".equals(b);
    }

    public int getInt(final String key, final int defaultValue) {
        try {
            return Integer.parseInt(getString(key, "not a number"));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public long getLong(final String key, final long defaultValue) {
        try {
            return Long.parseLong(getString(key, "not a number"));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public double getDouble(final String key, final double defaultValue) {
        try {
            return Double.parseDouble(getString(key, "not a number"));
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
