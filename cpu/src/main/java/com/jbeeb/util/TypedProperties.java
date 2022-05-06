package com.jbeeb.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypedProperties {

    private final Map<String, String> map = new HashMap<>();
    private final Map<String, int[]> arrayMap = new HashMap<>();

    public final void putString(final String key, final String value) {
        map.put(Objects.requireNonNull(key), value);
    }

    public final boolean containsKey(String key) {
        return map.containsKey(key) || arrayMap.containsKey(key);
    }

    public final void putBoolean(final String key, final boolean value) {
        putString(key, Boolean.toString(value));
    }

    public final void putInt(final String key, final int value) {
        putString(key, Integer.toString(value));
    }

    public final void putLong(final String key, final long value) {
        putString(key, Long.toString(value));
    }

    public final void putDouble(final String key, final double value) {
        putString(key, Double.toString(value));
    }

    public final void putIntArray(final String key, final int[] array) {
        arrayMap.put(key, Arrays.copyOf(array, array.length));
    }

    public final String getString(final String key, final String defaultValue) {
        return map.getOrDefault(Objects.requireNonNull(key), defaultValue);
    }

    public final boolean getBoolean(final String key, final boolean defaultValue) {
        final String b = getString(key, Boolean.toString(defaultValue));
        return "true".equals(b);
    }

    public final int getInt(final String key, final int defaultValue) {
        try {
            return Integer.parseInt(getString(key, "not a number"));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public final long getLong(final String key, final long defaultValue) {
        try {
            return Long.parseLong(getString(key, "not a number"));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public final double getDouble(final String key, final double defaultValue) {
        try {
            return Double.parseDouble(getString(key, "not a number"));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public final int[] getIntArray(final String key) {
        final int[] arr = arrayMap.get(key);
        return Arrays.copyOf(arr, arr.length);
    }

    public final void write(final DataOutput out) throws IOException {
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

    public static TypedProperties read(final DataInput in) throws IOException {
        final int mapSize = in.readInt();
        final TypedProperties ret = new TypedProperties();
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
