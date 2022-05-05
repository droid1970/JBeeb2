package com.jbeeb.localfs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public final class DiskImage implements LfsElement{

    private final LfsElement parent;
    private final Map<FileMetadata, byte[]> files;
    private final ByteBuffer data;

    public DiskImage(final LfsElement parent, final File file) throws IOException {
        this.parent = parent;
        final byte[] bytes = new byte[(int) file.length()];
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            int i = 0;
            int v;
            while ((v = in.read()) >= 0) {
                bytes[i++] = (byte) v;
            }
        }
        this.data = ByteBuffer.wrap(bytes);
        this.files = getFiles(data);
    }

    private static final class FileElement implements LfsElement {
        private final LfsElement parent;
        private final FileMetadata metadata;
        private final byte[] data;

        public FileElement(LfsElement parent, FileMetadata metadata, byte[] data) {
            this.parent = parent;
            this.metadata = metadata;
            this.data = data;
        }

        @Override
        public String getName() {
            return metadata.name;
        }

        @Override
        public Optional<? extends LfsElement> getParent() {
            return Optional.of(parent);
        }

        @Override
        public List<? extends LfsElement> list() {
            return Collections.emptyList();
        }

        @Override
        public boolean isDirectory() {
            return false;
        }
    }

    @Override
    public String getName() {
        return getDiskName();
    }

    @Override
    public Optional<? extends LfsElement> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public List<? extends LfsElement> list() {
        return files.keySet().stream().map(f -> new FileElement(this, f, null)).collect(Collectors.toList());
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    private static final class FileMetadata {
        final String directory;
        final String name;
        final int loadAddress;
        final int execAddress;
        final int length;
        final int startSector;

        public FileMetadata(String directory, String name, int loadAddress, int execAddress, int length, int startSector) {
            this.directory = directory;
            this.name = name;
            this.loadAddress = loadAddress;
            this.execAddress = execAddress;
            this.length = length;
            this.startSector = startSector;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileMetadata filename = (FileMetadata) o;
            return Objects.equals(directory, filename.directory) && Objects.equals(name, filename.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(directory, name);
        }
    }

    private static Map<FileMetadata, byte[]> getFiles(final ByteBuffer data) {
        final Map<FileMetadata, byte[]> ret = new HashMap<>();
        for (int i = 1; i < 32; i++) {
            // Get filename and directory name
            data.position(i * 8);
            final byte[] nameBytes = new byte[8];
            data.get(nameBytes);
            if (allZeros(nameBytes)) {
                break;
            }
            data.position(256 + i * 8);
            final byte[] metaBytes = new byte[8];
            data.get(metaBytes);

            final int length = getLength(metaBytes);
            final int loadAddress = getLoadAddress(metaBytes);
            final int execAddress = getExecAddress(metaBytes);
            final int startSector = getStartSector(metaBytes);

            final String name = new String(nameBytes, 0, 7, StandardCharsets.UTF_8).trim();
            final boolean locked = (nameBytes[7] & 0x80) != 0;
            nameBytes[7] &= 0x7F;
            final String directory = new String(nameBytes, 7, 1);
            ret.put(new FileMetadata(directory, name, loadAddress, execAddress, length, startSector), null);
        }
        return ret;
    }

    private static int getLoadAddress(final byte[] metaBytes) {
        int ret = metaBytes[0] & 0xFF;
        ret |= (metaBytes[1] & 0xFF) << 8;
        //ret |= (metaBytes[6] & 0xC) << 14;
        return ret;
    }

    private static int getExecAddress(final byte[] metaBytes) {
        int ret = metaBytes[2] & 0xFF;
        ret |= (metaBytes[3] & 0xFF) << 8;
        //ret |= (metaBytes[6] & 0xC0) << 10;
        return ret;
    }

    private static int getLength(final byte[] metaBytes) {
        int ret = metaBytes[4] & 0xFF;
        ret |= (metaBytes[5] & 0xFF) << 8;
        ret |= (metaBytes[6] & 0x30) << 12;
        return ret;
    }

    private static int getStartSector(final byte[] metaBytes) {
        return ((metaBytes[6] & 0x3) << 8) | (metaBytes[7] & 0xFF);
    }

    private static boolean allZeros(final byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public String getDiskName() {
        final byte[] bytes = new byte[12];
        data.position(0);
        data.get(bytes, 0, 8);
        data.position(256);
        data.get(bytes, 8, 4);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }
}
