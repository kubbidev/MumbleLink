package me.kubbidev.mumble.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Key {

    @Environment(EnvType.CLIENT)
    interface Identity {

        String NAME        = "name";
        String DIMENSION   = "dimension";
        String WORLD_SPAWN = "worldSpawn";
    }

    @Environment(EnvType.CLIENT)
    interface Context {

        String DOMAIN = "domain";
    }
}
