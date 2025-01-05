package me.kubbidev.mumble.loader;

import org.jetbrains.annotations.NotNull;
import me.kubbidev.mumble.jna.LinkApi;

/**
 * Represents a functional interface for loading APIs with a given name.
 */
@FunctionalInterface
public interface ApiLoader {

    /**
     * Loads and provides a link to the API with the specified name.
     *
     * @param name the name of the API to be loaded
     * @return a {@link LinkApi} instance representing the loaded API
     * @throws UnsatisfiedLinkError if the API cannot be loaded
     */
    @NotNull LinkApi load(String name) throws UnsatisfiedLinkError;
}
