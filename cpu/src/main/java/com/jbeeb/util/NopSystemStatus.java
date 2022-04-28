package com.jbeeb.util;

final class NopSystemStatus implements SystemStatus {

    @Override
    public void putString(String key, String value) {
        // Do nothing
    }

    @Override
    public String getString(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        // Do nothing
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public void putInt(String key, int value) {
        // Do nothing
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public void putLong(String key, long def) {
        // Do nothing
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return defaultValue;
    }

    @Override
    public void putDouble(String key, double value) {
        // Do nothing
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return defaultValue;
    }
}
