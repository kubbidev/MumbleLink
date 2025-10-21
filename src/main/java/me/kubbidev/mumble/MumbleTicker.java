package me.kubbidev.mumble;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import me.kubbidev.mumble.api.Module;

@Environment(EnvType.CLIENT)
public class MumbleTicker implements Module, ClientTickEvents.EndTick {

    private final MumbleLoader loader;
    private       boolean      enabled = false;

    public MumbleTicker(MumbleLoader loader) {
        this.loader = loader;
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (enabled) {
            loader.onEndTick(client);
        }
    }

    @Override
    public void enable() {
        enabled = true;
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }

    @Override
    public void disable() {
        enabled = false;
    }
}
