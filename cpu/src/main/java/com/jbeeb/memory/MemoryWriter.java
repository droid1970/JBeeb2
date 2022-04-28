package com.jbeeb.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MemoryWriter {

    private final Memory memory;
    private final Map<String, Integer> labels = new HashMap<>();

    private int pos;

    public MemoryWriter(final Memory memory) {
        this.memory = Objects.requireNonNull(memory);
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void writeByteToPos(int value) {
        memory.writeByte(pos++, value);
    }

    public void writeBytesToPos(List<Integer> values) {
        values.forEach(this::writeByteToPos);
    }

    public void markPos(final String labelName) {
        mark(labelName, pos);
    }

    public void mark(final String labelName, final int address) {
        labels.put(labelName, address);
    }

}
