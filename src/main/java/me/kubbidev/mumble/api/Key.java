package me.kubbidev.mumble.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
@Environment(EnvType.CLIENT)
public interface Key {

    @ApiStatus.NonExtendable
    @Environment(EnvType.CLIENT)
    interface Identity {

        @ApiStatus.Internal
        String NAME = "name";

        @ApiStatus.Internal
        String DIMENSION = "dimension";

        @ApiStatus.Internal
        String WORLD_SPAWN = "worldSpawn";
    }

    @ApiStatus.NonExtendable
    @Environment(EnvType.CLIENT)
    interface Context {

        @ApiStatus.Internal
        String DOMAIN = "domain";
    }
}
