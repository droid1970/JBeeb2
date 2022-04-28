package com.jbeeb.util;

public interface SystemStatus {

    String KEY_TOTAL_CYCLES = "total-cycles";
    String KEY_UP_TIME = "up-time";
    String KEY_MILLION_CYCLES_PER_SECOND = "mega-cycles-per-second";
    String KEY_AVG_DISPLAY_REFRESH_TIME_MILLIS = "avg-display-refresh-time-millis";

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
