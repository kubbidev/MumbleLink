package me.kubbidev.mumble.loader;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.kubbidev.mumble.MumbleLinkMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.kubbidev.mumble.jna.LinkApi;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class StructureLoader {

    /**
     * A mapping that associates platform-specific identifiers with corresponding library folder names.
     */
    private static final Int2ObjectMap<String> PLATFORM_FOLDERS_MAPPING;
    /**
     * A mapping that associates platform-specific identifiers with corresponding library file names.
     */
    private static final Int2ObjectMap<String> PLATFORM_FILES_MAPPING;

    static {
        PLATFORM_FOLDERS_MAPPING = Int2ObjectMaps.unmodifiable(new Int2ObjectOpenHashMap<>(Map.of(
            Platform.LINUX, "linux", Platform.WINDOWS, "win32", Platform.MAC, "darwin"
        )));

        PLATFORM_FILES_MAPPING = Int2ObjectMaps.unmodifiable(new Int2ObjectOpenHashMap<>(Map.of(
            Platform.LINUX, "lib%1s.so", Platform.WINDOWS, "%1s.dll", Platform.MAC, "lib%1s.dylib"
        )));
    }

    private StructureLoader() {
    }

    /**
     * Instantiates the {@link LinkApi} structure by loading the specified native library.
     * <p>
     * This method extracts the required library to a temporary file, adds its path to the native library search path, and then attempts to
     * load and bind it to the {@link LinkApi} interface.
     *
     * @param name the name of the native library to be loaded
     * @return an instance of the LinkApi interface bound to the loaded library
     * @throws LoadingException if the library cannot be located, extracted, or loaded
     */
    public static LinkApi instantiateStructure(String name) throws LoadingException {
        // Extract the library structure to a temporary file path.
        var unpackedFile = extractStructure(name);
        if (Files.exists(unpackedFile)) {
            NativeLibrary.addSearchPath(name, unpackedFile.toString());
        }

        // Attempt to get an instance of the native library using its name.
        var loadedLibrary = NativeLibrary.getInstance(name);
        if (loadedLibrary == null) {
            throw new LoadingException("Unable to locate library");
        }

        // Load the library and bind it to the LinkApi interface.
        var linkApi = Native.load(name, LinkApi.class);
        if (linkApi != null) {
            return linkApi;
        } else {
            // Throw an exception if the library cannot be loaded.
            throw new LoadingException("Required library could not be loaded");
        }
    }

    private static Path extractStructure(String name) throws LoadingException {
        int osType = Platform.getOSType();

        // Retrieve the file name for the current platform using
        // a predefined mapping.
        var file = String.format(PLATFORM_FILES_MAPPING.get(osType), name);

        // Initialize the folder path based on the operating system using
        // a predefined mapping.
        var folder = new StringBuilder(PLATFORM_FOLDERS_MAPPING.get(osType));

        // Append the appropriate architecture information if the OS is
        // not macOS.
        if (osType != Platform.MAC) {
            String arch = Platform.is64Bit() ? "x86-64" : "x86";
            folder.append('-').append(arch);
        }

        // Extract the file from the appropriate folder path.
        return extractFile("/" + folder + "/" + file);
    }

    /**
     * Extracts a file from the given resource path to a temporary location.
     *
     * <p>The file is guaranteed to be marked for deletion upon JVM exit.</p>
     *
     * @param resourcePath the path of the resource to be extracted
     * @return the temporary file path where the resource has been extracted
     * @throws LoadingException if the resource cannot be located, the temporary file cannot be created, or the resource cannot be written
     *                          to the temporary file
     */
    private static Path extractFile(String resourcePath) throws LoadingException {
        var structure = MumbleLinkMod.MOD_CONTAINER.findPath(resourcePath)
            .orElseThrow(() -> new LoadingException("Could not locate : " + resourcePath));

        // create a temporary file
        // on posix systems by default this is only read/writable by the process owner
        Path path;
        try {
            path = Files.createTempFile("mumblelink-structure", ".tmp");
        } catch (IOException e) {
            throw new LoadingException("Unable to create a temporary file", e);
        }

        // mark that the file should be deleted on exit
        path.toFile().deleteOnExit();

        // copy the structure to the temporary file path
        try (InputStream in = Files.newInputStream(structure)) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new LoadingException("Unable to copy structure to temporary path", e);
        }

        return path.toAbsolutePath();
    }
}
