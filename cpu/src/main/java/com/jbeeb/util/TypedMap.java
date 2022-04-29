package com.jbeeb.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TypedMap {

    private final Map<String, String> map = new HashMap<>();
    private final Map<String, int[]> arrayMap = new HashMap<>();

    public void putString(final String key, final String value) {
        map.put(Objects.requireNonNull(key), value);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key) || arrayMap.containsKey(key);
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

    public void putIntArray(final String key, final int[] array) {
        arrayMap.put(key, Arrays.copyOf(array, array.length));
    }

    public String getString(final String key, final String defaultValue) {
        return map.getOrDefault(Objects.requireNonNull(key), defaultValue);
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

    public int[] getIntArray(final String key) {
        final int[] arr = arrayMap.get(key);
        return Arrays.copyOf(arr, arr.length);
    }

    public void write(final DataOutput out) throws IOException {
        out.writeInt(map.size());

        for (Map.Entry<String, String> e : map.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }

        out.writeInt(arrayMap.size());
        for (Map.Entry<String, int[]> e : arrayMap.entrySet()) {
            out.writeUTF(e.getKey());
            writeArray(out, e.getValue());
        }
    }

    public static TypedMap read(final DataInput in) throws IOException {
        final int mapSize = in.readInt();
        final TypedMap ret = new TypedMap();
        for (int i = 0; i < mapSize; i++) {
            ret.map.put(in.readUTF(), in.readUTF());
        }
        final int arrayMapSize = in.readInt();
        for (int i = 0; i < arrayMapSize; i++) {
            ret.arrayMap.put(in.readUTF(), readArray(in));
        }
        return ret;
    }

    private static void writeArray(final DataOutput out, final int[] array) throws IOException {
        out.writeInt(array.length);
        for (int i : array) {
            out.writeInt(i);
        }
    }

    private static int[] readArray(final DataInput in) throws IOException {
        final int[] array = new int[in.readInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = in.readInt();
        }
        return array;
    }
}
