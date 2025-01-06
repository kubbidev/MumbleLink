package me.kubbidev.mumble.jna;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class LinkApiHelper {
    private LinkApiHelper() {}

    public static @NotNull CharBuffer parseToCharBuffer(
            int capacity, @NotNull String value
    ) {
        CharBuffer buffer = CharBuffer.allocate(capacity);
        buffer.rewind();
        buffer.put(Objects.requireNonNull(value, "value").toCharArray());
        buffer.rewind();
        return buffer;
    }

    public static @NotNull ByteBuffer parseToByteBuffer(
            int capacity, @NotNull String value
    ) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.rewind();
        buffer.put(Objects.requireNonNull(value, "value").getBytes());
        buffer.rewind();
        return buffer;
    }
}
