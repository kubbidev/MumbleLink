package me.kubbidev.mumble.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Module {

    void enable();

    void disable();
}
