package com.jbeeb.util;

import com.jbeeb.assembler.Assembler;
import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.Flag;
import com.jbeeb.cpu.InstructionSet;
import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.VideoULA;
import com.jbeeb.screen.DisplayMode;
import com.jbeeb.memory.Memory;

import java.lang.annotation.Annotation;
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
        final boolean carrtOut = (a & 0x80) != 0;
        final int carry = (carryIn) ? 1 : 0;
        final int result = ((a << 1) & 0xFF) | carry;
        cpu.setFlag(Flag.CARRY, carrtOut);
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

    public static int subtractWithCarry(final Cpu cpu, final int a, final int b, final boolean carryIn) {
        return addWithCarry(cpu, a, Util.onesComplement(b), carryIn);
    }

    public static void cmp(final Cpu cpu, final int register, final int value) {
        final int result = (register - value) & 0xFF;
        if (register < value) {
            cpu.setFlag(Flag.ZERO, false);
            cpu.setFlag(Flag.CARRY, false);
            cpu.setFlag(Flag.NEGATIVE, (result & 0x80) != 0);
        } else if (register > value) {
            cpu.setFlag(Flag.ZERO, false);
            cpu.setFlag(Flag.CARRY, true);
            cpu.setFlag(Flag.NEGATIVE, (result & 0x80) != 0);
        } else {
            cpu.setFlag(Flag.ZERO, true);
            cpu.setFlag(Flag.CARRY, true);
            cpu.setFlag(Flag.NEGATIVE, false);
        }
    }

    public static Cpu createCpu(final int codeStart, final String... statements) {
        final Memory memory = Memory.randomAccessMemory(0, 65536);
        memory.writeWord(Cpu.CODE_START_VECTOR, codeStart);
        final InstructionSet instructionSet = new InstructionSet();
        final Assembler assembler = new Assembler(codeStart, memory, instructionSet);
        assembler.assemble(statements);
        assembler.assemble("HLT");
        return new Cpu(SystemStatus.NOP, memory).setVerboseSupplier(null);
    }

    public static Cpu runCpu(final int codeStart, final String... statements) {
        final Cpu cpu = createCpu(codeStart, statements);
        cpu.run();
        return cpu;
    }

    public static DisplayMode inferDisplayMode(final VideoULA videoULA, final CRTC6845 crtc6845) {
        if (videoULA.isTeletext()) {
            return DisplayMode.MODE7;
        }

        final int horizontalChars = videoULA.getCharactersPerLine();
        final int verticalChars = crtc6845.getVerticalDisplayedChars();

        if (horizontalChars == 20) {
            return videoULA.isFastClockRate() ? DisplayMode.MODE2 : DisplayMode.MODE5;
        } else if (horizontalChars == 40) {
            if (videoULA.isFastClockRate()) {
                return DisplayMode.MODE1;
            } else {
                return (verticalChars == 32) ? DisplayMode.MODE4 : DisplayMode.MODE6;
            }
        } else if (horizontalChars == 80) {
            return (verticalChars == 32) ? DisplayMode.MODE0 : DisplayMode.MODE3;
        } else {
            return null;
        }
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

    public static String pad(final String s, int width) {
        final StringBuilder sb = new StringBuilder();
        sb.append(s);
        while (sb.length() < width) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String padLeft(final String s, int width) {
        final StringBuilder sb = new StringBuilder();
        sb.append(s);
        while (sb.length() < width) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String pad0(final String s, final int width) {
        return padLeft(s, width, '0');
    }

    public static String padLeft(final String s, final int width, final char padChar) {
        final StringBuilder sb = new StringBuilder();
        final int padCount = width - s.length();
        for (int i = 0; i < padCount; i++) {
            sb.append(padChar);
        }
        sb.append(s);
        return sb.toString();
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

    private static void populateTypeMap(final TypedMap typedMap, final Object obj, final List<Field> fields) throws Exception {
        for (Field f : fields) {
            final Annotation a = f.getAnnotation(StateKey.class);
            if (a != null) {
                final String key = ((StateKey) a).key();
                f.setAccessible(true);
                final Class<?> type = f.getType();
                final Object value = f.get(obj);
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
        final Class<?> cl = obj.getClass();
        if (cl.getAnnotation(StateKey.class) != null) {
            final String key = ((StateKey) cl.getAnnotation(StateKey.class)).key();
            final List<Field> fields = new ArrayList<>();
            getStateFields(fields, cl);
            final TypedMap typedMap = new TypedMap();
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
            final String key = ((StateKey) cl.getAnnotation(StateKey.class)).key();
            final TypedMap typedMap = state.get(key);
            if (typedMap != null) {
                final List<Field> fields = new ArrayList<>();
                getStateFields(fields, cl);
                for (Field f : fields) {
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
}
