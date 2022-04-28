package com.jbeeb.util;

public interface SystemStatus {

    String KEY_MILLION_CYCLES_PER_SECOND = "mega-cycles-per-second";

    SystemStatus NOP = new NopSystemStatus();

    void putString(String key, String value);
    String getString(String key, String defaultValue);

    void putBoolean(String key, boolean value);
    boolean getBoolean(String key, boolean defaultValue);

    void putInt(String key, int value);
    int getInt(String key, int defaultValue);

    void putLong(String key, long value);
    long getLong(String key, long defaultValue);

    void putDouble(String key, double value);
    double getDouble(String key, double defaultValue);
}
