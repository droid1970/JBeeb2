package com.jbeeb.util;

import com.jbeeb.assembler.Assembler;
import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.Flag;
import com.jbeeb.cpu.InstructionSet;
import com.jbeeb.disk.DiskDetails;
import com.jbeeb.memory.Memory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Util {

    private static final NumberFormat DURATION_FORMAT = new DecimalFormat("0.00");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static int checkUnsignedByte(final int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(value + ": value must be >=0 and <= 255");
        }
        return value;
    }

    public static int checkUnsignedWord(final int value) {
        if (value < 0 || value >= 65535) {
            throw new IllegalArgumentException(value + ": value must be >=0 and <= 65535");
        }
        return value;
    }

    public static int signed(final int value) {
        checkUnsignedByte(value);
        return (byte) value;
    }

    public static boolean isNegative(final int value) {
        return ((value & 0x80) != 0);
    }

    public static boolean isZero(final int value) {
        return value == 0;
    }

    public static int onesComplement(final int value) {
        return (~value & 0xFF);
    }

    public static int twosComplement(final int value) {
        return onesComplement(value) + 1;
    }

    public static int and(final Cpu cpu, final int a, final int b) {
        return a & b;
    }

    public static int eor(final Cpu cpu, final int a, final int b) {
        return a ^ b;
    }

    public static int asl(final Cpu cpu, final int a) {
        final boolean carryOut = (a & 0x80) != 0;
        final int result = (a << 1) & 0xFF;
        cpu.setFlag(Flag.CARRY, carryOut);
        cpu.maintainNZ(result);
        return result;
    }

    public static int lsr(final Cpu cpu, final int a) {
        final boolean carryOut = (a & 1) != 0;
        final int result = (a >>> 1) & 0xFF;
        cpu.setFlag(Flag.CARRY, carryOut);
        cpu.maintainNZ(result);
        return result;
    }

    public static int rol(final Cpu cpu, final int a, final boolean carryIn) {
        final boolean carryOut = (a & 0x80) != 0;
        final int carry = (carryIn) ? 1 : 0;
        final int result = ((a << 1) & 0xFF) | carry;
        cpu.setFlag(Flag.CARRY, carryOut);
        cpu.maintainNZ(result);
        return result;
    }

    public static int ror(final Cpu cpu, final int a, final boolean carryIn) {
        final boolean carrtOut = (a & 1) != 0;
        final int carry = (carryIn) ? 0x80 : 0;
        final int result = ((a >>> 1) & 0xFF) | carry;
        cpu.setFlag(Flag.CARRY, carrtOut);
        cpu.maintainNZ(result);
        return result;
    }

    public static int inc(final Cpu cpu, final int a) {
        final int result = (a + 1) & 0xFF;
        cpu.maintainNZ(result);
        return result;
    }

    public static int dec(final Cpu cpu, final int a) {
        final int result = (a - 1) & 0xFF;
        cpu.maintainNZ(result);
        return result;
    }

    public static int or(final Cpu cpu, final int a, final int b) {
        return a | b;
    }

    public static int addWithCarry(final Cpu cpu, final int a, final int b, final boolean carryIn) {
        checkUnsignedByte(a);
        checkUnsignedByte(b);
        int result = a + b + (carryIn ? 1 : 0);
        cpu.setFlag(Flag.CARRY, (result & 0x100) != 0);
        cpu.setFlag(Flag.OVERFLOW, ((a ^ result) & (b ^ result) & 0x80) != 0);
        return result & 0xFF;
    }

    public static int addWithCarryBCD(final Cpu cpu, final int a, final int addend, final boolean carryIn) {
        int ah = 0;
        int tempb = (a + addend + (carryIn ? 1 : 0)) & 0xFF;
        cpu.setFlag(Flag.ZERO, tempb == 0);
        int al = (a & 0xF) + (addend & 0xF) + (carryIn ? 1 : 0);
        if (al > 9) {
            al -= 10;
            al &= 0xF;
            ah = 1;
        }
        ah += (a >>> 4) + (addend >>> 4);
        cpu.setFlag(Flag.NEGATIVE, ((ah & 8) != 0));
        cpu.setFlag(Flag.OVERFLOW, ((a ^ addend) & 0x80) != 0 && (((a ^ (ah << 4)) & 0x80) != 0));
        cpu.setFlag(Flag.CARRY, false);
        if (ah > 9) {
            cpu.setFlag(Flag.CARRY, true);
            ah -= 10;
            ah &= 0xFF;
        }

        return ((al & 0xF) | (ah << 4)) & 0xFF;
    }

    public static int subtractWithCarryBCD(final Cpu cpu, final int a, final int subend, final boolean carryIn) {
        int carry = (carryIn) ? 0 : 1;
        int al = (a & 0xF) - (subend & 0xF) - carry;
        int ah = (a >>> 4) - (subend >>> 4);
        if ((al & 0x10) != 0) {
            al = (al - 6) & 0xf;
            ah--;
        }
        if ((ah & 0x10) != 0) {
            ah = (ah - 6) & 0xF;
        }

        int result = a - subend - carry;
        cpu.setFlag(Flag.NEGATIVE, (result & 0x80) != 0);
        cpu.setFlag(Flag.ZERO, (result & 0xFF) == 0);
        cpu.setFlag(Flag.OVERFLOW, (((a ^ result) & (subend ^ a) ^ 0x80)) != 0);
        cpu.setFlag(Flag.CARRY, (result & 0x100) == 0);
        return al | (ah << 4);
    }

    public static int subtractWithCarry(final Cpu cpu, final int a, final int b, final boolean carryIn) {
        return addWithCarry(cpu, a, Util.onesComplement(b), carryIn);
    }

    public static void cmp(final Cpu cpu, final int register, final int value) {
        final int result = (register - value) & 0xFF;
        cpu.setFlag(Flag.ZERO, result == 0);
        cpu.setFlag(Flag.NEGATIVE, (result & 0x80) != 0);
        cpu.setFlag(Flag.CARRY, register >= value);
    }

    public static Cpu createCpu(final int codeStart, final String... statements) {
        final Memory memory = Memory.randomAccessMemory(0, 65536);
        memory.writeWord(Cpu.CODE_START_VECTOR, codeStart);
        final InstructionSet instructionSet = new InstructionSet();
        final Assembler assembler = new Assembler(codeStart, memory, instructionSet);
        assembler.assemble(statements);
        assembler.assemble("HLT");
        return new Cpu(SystemStatus.NOP, new DefaultScheduler(), memory).setVerboseSupplier(null);
    }

    public static Cpu runCpu(final int codeStart, final String... statements) {
        final Cpu cpu = createCpu(codeStart, statements);
        cpu.run();
        return cpu;
    }

    public static String formatHexByte(final int n) {
        String s = Integer.toHexString(n);
        if (s.length() == 1) {
            s = "0" + s;
        }
        return "$" + s.toUpperCase();
    }

    public static String formatHexWord(final int n) {
        String s = Integer.toHexString(n);
        while (s.length() < 4) {
            s = "0" + s;
        }
        return "$" + s.toUpperCase();
    }

    public static String padRight(final String s, int width) {
        final StringBuilder sb = new StringBuilder();
        sb.append(s);
        while (sb.length() < width) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String padLeft(final String s, final int width, final char padChar) {
        final StringBuilder sb = new StringBuilder();
        final int padCount = width - s.length();
        sb.append(String.valueOf(padChar).repeat(padCount));
        sb.append(s);
        return sb.toString();
    }

    public static String padLeft(final String s, int width) {
        return padLeft(s, width, ' ');
    }

    public static String pad0(final String s, final int width) {
        return padLeft(s, width, '0');
    }

    public static String formatDateTime(final LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static void log(String message, long cycle) {
        System.err.println(formatDateTime(LocalDateTime.now()) + ":" + padLeft("" + cycle, 10, ' ') + " - " + message);
    }

    public static String formatDurationNanosAsMillis(final long nanos) {
        final double secs = (double) nanos / 1_000_000L;
        return DURATION_FORMAT.format(secs);
    }

    private static void getStateFields(final List<Field> fields, final Class<?> cl) throws Exception {
        if (cl == Object.class) {
            return;
        }

        for (Field f : cl.getDeclaredFields()) {
            if (f.getAnnotation(StateKey.class) != null) {
                fields.add(f);
            }
        }

        getStateFields(fields, cl.getSuperclass());
    }

    private static void populateTypeMap(final TypedProperties typedMap, final Object obj, final List<Field> fields) throws Exception {
        for (Field f : fields) {
            final var a = f.getAnnotation(StateKey.class);
            if (a != null) {
                final String key = a.key();
                f.setAccessible(true);
                final var type = f.getType();
                final var value = f.get(obj);
                if (type == int.class) {
                    typedMap.putInt(key, (int) value);
                } else if (type == long.class) {
                    typedMap.putLong(key, (long) value);
                } else if (type == boolean.class) {
                    typedMap.putBoolean(key, (boolean) value);
                } else if (type == int[].class) {
                    typedMap.putIntArray(key, (int[]) value);
                } else if (type == double.class) {
                    typedMap.putDouble(key, (double) value);
                } else if (type == String.class) {
                    typedMap.putString(key, (String) value);
                }
            }
        }
    }

    public static void populateState(final State state, Object obj) throws Exception {
        final var cl = obj.getClass();
        if (cl.getAnnotation(StateKey.class) != null) {
            final var key = cl.getAnnotation(StateKey.class).key();
            final List<Field> fields = new ArrayList<>();
            getStateFields(fields, cl);
            final var typedMap = new TypedProperties();
            if (!fields.isEmpty()) {
                populateTypeMap(typedMap, obj, fields);
            }
            state.put(key, typedMap);
        }
        int x = 1;
    }

    public static void applyState(final State state, final Object obj) throws Exception {
        final Class<?> cl = obj.getClass();
        if (cl.getAnnotation(StateKey.class) != null) {
            final var key = cl.getAnnotation(StateKey.class).key();
            final var typedMap = state.get(key);
            if (typedMap != null) {
                final List<Field> fields = new ArrayList<>();
                getStateFields(fields, cl);
                for (var f : fields) {
                    f.setAccessible(true);
                    final String fieldKey = f.getAnnotation(StateKey.class).key();
                    if (typedMap.containsKey(fieldKey)) {
                        final Class<?> type = f.getType();
                        if (type == int.class) {
                            f.set(obj, typedMap.getInt(fieldKey, 0));
                        } else if (type == long.class) {
                            f.set(obj, typedMap.getLong(fieldKey, 0));
                        } else if (type == boolean.class) {
                            f.set(obj, typedMap.getBoolean(fieldKey, false));
                        } else if (type == double.class) {
                            f.set(obj, typedMap.getDouble(fieldKey, 0));
                        } else if (type == String.class) {
                            f.set(obj, typedMap.getString(fieldKey, ""));
                        } else if (type == int[].class) {
                            f.set(obj, typedMap.getIntArray(fieldKey));
                        }
                    }
                }
            }
        }
    }

    public static void run(final Cpu cpu, final Memory memory, final File file, final int loadAddress, final int execAddress) throws IOException {
        if (file.exists()) {
            final int[] data = readFileAsInts(file);
            for (int i = 0; i < data.length; i++) {
                memory.writeByte(loadAddress + i, data[i]);
            }
            cpu.setQuiescentCallback(() -> {
                cpu.setPC(execAddress);
            });
        }
    }

    public static void load(final Cpu cpu, final Memory memory, final File file, final int loadAddress) throws IOException {
        if (file.exists()) {
            final int[] data = readFileAsInts(file);
            for (int i = 0; i < data.length; i++) {
                memory.writeByte(loadAddress + i, data[i]);
            }
        }
    }

    public static int[] readFileAsInts(final File file) throws IOException {
        final long size = file.length();
        final int[] ret = new int[(int) size];
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            int i = 0;
            while (true) {
                final int b = in.read();
                if (b < 0) {
                    break;
                }
                ret[i] = b & 0xFF;
                i++;
            }
        }
        return ret;
    }

    public static byte[] readFileAsbytes(final File file) throws IOException {
        final long size = file.length();
        final byte[] ret = new byte[(int) size];
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            int i = 0;
            while (true) {
                final int b = in.read();
                if (b < 0) {
                    break;
                }
                ret[i] = (byte) b;
                i++;
            }
        }
        return ret;
    }

    public static DiskDetails getDiskDetails(final String name) {
        boolean dsd = false;
        int size = 80 * 10 * 256;
        if (name != null && name.toLowerCase().endsWith(".dsd")) {
            dsd = true;
            size *= 2;
        }
        return new DiskDetails(dsd, size);
    }

    public static void fillRect(final BufferedImage img, final int rgb, final int x, final int y, final int width, final int height) {
        for (int rx = x; rx < x + width; rx++) {
            for (int ry = y; ry < y + height; ry++) {
                img.setRGB(rx, ry, rgb);
            }
        }
    }

    public static void fillRect(final DataBuffer buf, final int rgb, final int x, final int y, final int width, final int height, final int imageWidth) {
        for (int rx = x; rx < x + width; rx++) {
            for (int ry = y; ry < y + height; ry++) {
                int i = (ry * imageWidth) + (rx % imageWidth);
                buf.setElem(i, rgb);
            }
        }
    }

    public static void fillRectXOR(final BufferedImage img, final int rgb, final int x, final int y, final int width, final int height) {
        for (int rx = x; rx < x + width; rx++) {
            for (int ry = y; ry < y + height; ry++) {
                img.setRGB(rx, ry, ((img.getRGB(rx, ry) & 0xFFFFFF) ^ (rgb & 0xFFFFFF)) | 0xFF000000);
            }
        }
    }

    public static void fillRectXOR(final DataBuffer buf, final int rgb, final int x, final int y, final int width, final int height, final int imageWidth) {
        for (int rx = x; rx < x + width; rx++) {
            for (int ry = y; ry < y + height; ry++) {
                final int i = (ry * imageWidth) + (rx % imageWidth);
                buf.setElem(i, ((buf.getElem(i) & 0xFFFFFF) ^ (rgb & 0xFFFFFF)) | 0xFF000000);
            }
        }
    }

    public static int[] resizeArray(final int[] array, final int size) {
        final int[] ret = new int[size];
        System.arraycopy(array, 0, ret, 0, Math.min(array.length, size));
        return ret;
    }

    public static int[] toIntArray(final byte[] bytes, final int size) {
        final int[] ret = new int[size];
        for (int i = 0; i < size; i++) {
            ret[i] = (i < bytes.length) ? ((int) bytes[i]) & 0xFF : 0;
        }
        return ret;
    }

    public static boolean isPrintableCharacter(final char c) {
        return (c >= 32 && c <= 127);
    }

    public static byte[] stringToBytes(final String s) {
        final byte[] bytes = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (isPrintableCharacter(c)) {
                bytes[i] = (byte) c;
            } else {
                bytes[i] = (byte) 32;
            }
        }
        return bytes;
    }

    public static String firstElementOf(String s, final String separator) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        if (s.contains(separator)) {
            s = s.substring(0, s.indexOf(separator));
        }
        return s;
    }
}
