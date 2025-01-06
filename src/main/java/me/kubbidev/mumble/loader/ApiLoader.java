package me.kubbidev.mumble.loader;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import me.kubbidev.mumble.jna.LinkApi;

/**
 * Defines a functional interface responsible for loading an API and
 * returning an instance of the {@link LinkApi}.
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ApiLoader {

    /**
     * Loads the API and returns a {@link LinkApi} instance.
     *
     * @return a {@link LinkApi} instance representing the loaded API
     * @throws UnsatisfiedLinkError if the API cannot be loaded
     */
    @NotNull LinkApi load() throws UnsatisfiedLinkError;
}
