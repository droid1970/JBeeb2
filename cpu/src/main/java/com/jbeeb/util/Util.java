package com.jbeeb.util;

import com.jbeeb.assembler.Assembler;
import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.Flag;
import com.jbeeb.cpu.InstructionSet;
import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.VideoULA;
import com.jbeeb.screen.DisplayMode;
import com.jbeeb.memory.Memory;

import java.io.*;
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

//    // For flags and stuff see URLs like:
//    // http://www.visual6502.org/JSSim/expert.html?graphics=false&a=0&d=a900f86911eaeaea&steps=16
//    function adcBCD(addend) {
//        var ah = 0;
//        var tempb = (cpu.a + addend + (cpu.p.c ? 1 : 0)) & 0xff;
//        cpu.p.z = !tempb;
//        var al = (cpu.a & 0xf) + (addend & 0xf) + (cpu.p.c ? 1 : 0);
//        if (al > 9) {
//            al -= 10;
//            al &= 0xf;
//            ah = 1;
//        }
//        ah += (cpu.a >>> 4) + (addend >>> 4);
//        cpu.p.n = !!(ah & 8);
//        cpu.p.v = !((cpu.a ^ addend) & 0x80) && !!((cpu.a ^ (ah << 4)) & 0x80);
//        cpu.p.c = false;
//        if (ah > 9) {
//            cpu.p.c = true;
//            ah -= 10;
//            ah &= 0xf;
//        }
//        cpu.a = ((al & 0xf) | (ah << 4)) & 0xff;
//    }
//

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
//    // With reference to c64doc: http://vice-emu.sourceforge.net/plain/64doc.txt
//    // and http://www.visual6502.org/JSSim/expert.html?graphics=false&a=0&d=a900f8e988eaeaea&steps=18
//    function sbcBCD(subend) {
//        var carry = cpu.p.c ? 0 : 1;
//        var al = (cpu.a & 0xf) - (subend & 0xf) - carry;
//        var ah = (cpu.a >>> 4) - (subend >>> 4);
//        if (al & 0x10) {
//            al = (al - 6) & 0xf;
//            ah--;
//        }
//        if (ah & 0x10) {
//            ah = (ah - 6) & 0xf;
//        }
//
//        var result = cpu.a - subend - carry;
//        cpu.p.n = !!(result & 0x80);
//        cpu.p.z = !(result & 0xff);
//        cpu.p.v = !!((cpu.a ^ result) & (subend ^ cpu.a) & 0x80);
//        cpu.p.c = !(result & 0x100);
//        cpu.a = al | (ah << 4);
//    }

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

    public static void run(final Cpu cpu, final Memory memory, final File file, final int loadAddress, final int execAddress) throws IOException {
        if (file.exists()) {
            final int[] data = readFile(file);
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
            final int[] data = readFile(file);
            for (int i = 0; i < data.length; i++) {
                memory.writeByte(loadAddress + i, data[i]);
            }
        }
    }

    public static int[] readFile(final File file) throws IOException {
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

    private static FileMetadata readMetadata(final File file) throws IOException {
        final int[] data = readFile(file);
        for (int i = 0; i < data.length; i++) {
            final int b = data[i];
            System.err.println("b = " + b + " / " + Util.formatHexByte(b) + " / " + ((char) b));
        }
        int x = 1;
        return null;
    }

    private static final class FileMetadata {

        final String name;
        final int loadAddress;
        final int execAddress;

        public FileMetadata(String name, int loadAddress, int execAddress) {
            this.name = name;
            this.loadAddress = loadAddress;
            this.execAddress = execAddress;
        }
    }
}
