package me.kubbidev.mumble;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class MumbleTicker implements ClientTickEvents.EndTick {

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

    public void enable() {
        enabled = true;
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }

    public void disable() {
        enabled = false;
    }
}
