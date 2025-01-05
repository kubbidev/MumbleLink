package me.kubbidev.mumble.loader;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.kubbidev.mumble.MumbleLinkMod;
import me.kubbidev.mumble.jna.LinkApi;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

public class LinkApiLoader implements ApiLoader {
    public static final LinkApiLoader INSTANCE = new LinkApiLoader();

    /** A mapping that associates platform-specific identifiers with corresponding library folder names. */
    private static final Int2ObjectMap<String> PLATFORM_FOLDERS_MAPPING;

    /** A mapping that associates platform-specific identifiers with corresponding library file names. */
    private static final Int2ObjectMap<String> PLATFORM_FILES_MAPPING;

    static {
        PLATFORM_FOLDERS_MAPPING = Int2ObjectMaps.unmodifiable(new Int2ObjectOpenHashMap<>(Map.of(
                Platform.LINUX, "linux", Platform.WINDOWS, "win32", Platform.MAC, "darwin"
        )));

        PLATFORM_FILES_MAPPING = Int2ObjectMaps.unmodifiable(new Int2ObjectOpenHashMap<>(Map.of(
                Platform.LINUX, "lib%1s.so", Platform.WINDOWS, "%1s.dll", Platform.MAC, "lib%1s.dylib"
        )));
    }

    private LinkApiLoader() {}

    @Override
    public @NotNull LinkApi load(String name) throws UnsatisfiedLinkError {
        var unpackedFile = unpackLibrary(name);
        if (unpackedFile != null && unpackedFile.exists()) {
            NativeLibrary.addSearchPath(name, unpackedFile.getAbsolutePath());
        }

        var loadedLibrary = NativeLibrary.getInstance(name);
        if (loadedLibrary != null) {

            var libraryInstance = Native.load(name, LinkApi.class);
            if (libraryInstance != null) return libraryInstance;
        }

        throw new UnsatisfiedLinkError("Required library could not be loaded, available libraries are incompatible!");
    }

    private @Nullable File unpackLibrary(String name) {
        try {
            String file = getFile(name);
            return extractAsTemp('/' + getFolder() + '/' + file, file);
        } catch (IOException e) {
            MumbleLinkMod.LOGGER.warn("Error extracting resource!", e);
            return null;
        }
    }

    private @NotNull String getFile(String name) {
        return String.format(PLATFORM_FILES_MAPPING.get(Platform.getOSType()), name);
    }

    private @NotNull String getFolder() {
        StringBuilder builder = new StringBuilder();
        builder.append(PLATFORM_FOLDERS_MAPPING.get(Platform.getOSType()));

        // there should be a universal dylib for MAC so we can
        // skip the architecture part
        if (Platform.getOSType() == Platform.MAC) {
            return builder.toString();
        }

        String arch = "x86";
        if (Platform.is64Bit()) {
            arch = "x86_64";
        }

        builder.append('-');
        builder.append(arch);
        return builder.toString();
    }

    private File extractAsTemp(String resource, String fileName) throws IOException {
        File tempRoot = File.createTempFile("MumbleLink", "native");

        String tempPath = tempRoot.getAbsolutePath();
        if (!tempRoot.delete() || !new File(tempPath).mkdirs()) {
            throw new IOException("Failed to prepare temporary directory at : " + tempPath);
        }

        File tempFolder = new File(tempPath);
        File outputFile = new File(tempFolder, fileName);

        var resourcePath = MumbleLinkMod.MOD_CONTAINER.findPath(resource)
                .orElseThrow(() -> new FileNotFoundException("Resource not found : " + resource));

        try (var in = Files.newInputStream(resourcePath);
             var out = new FileOutputStream(outputFile)) {
            this.copyStream(in, out);
        }

        outputFile.deleteOnExit();
        tempFolder.deleteOnExit();
        return tempFolder;
    }

    /**
     * Copies the content of an InputStream to an OutputStream.
     *
     * @param in  The input stream to read from
     * @param out The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192]; // Buffer for efficient data transfer
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
}
