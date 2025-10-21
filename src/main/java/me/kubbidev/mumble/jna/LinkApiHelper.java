package me.kubbidev.mumble.jna;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class LinkApiHelper {

    private LinkApiHelper() {
    }

    public static CharBuffer parseToCharBuffer(int capacity, String value) {
        Objects.requireNonNull(value, "value");
        CharBuffer buffer = CharBuffer.allocate(capacity);
        buffer.rewind();
        buffer.put(value.toCharArray());
        buffer.rewind();
        return buffer;
    }

    public static ByteBuffer parseToByteBuffer(int capacity, String value) {
        Objects.requireNonNull(value, "value");
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.rewind();
        buffer.put(value.getBytes());
        buffer.rewind();
        return buffer;
    }
}
