package me.kubbidev.mumble.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public interface Identity {

    @ApiStatus.Internal
    String NAME = "name";

    @ApiStatus.Internal
    String WORLD = "world";

    @ApiStatus.Internal
    String WORLD_SPAWN = "worldSpawn";
}
