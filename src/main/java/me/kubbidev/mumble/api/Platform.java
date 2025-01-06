package me.kubbidev.mumble.api;

import me.kubbidev.mumble.MumbleLinkMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
public final class Platform {
    private Platform() {}

    @ApiStatus.Internal
    public static String getName() {
        return MumbleLinkMod.MOD_CONTAINER.getMetadata().getName();
    }

    @ApiStatus.Internal
    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

}
