package me.kubbidev.mumble;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import me.kubbidev.mumble.exception.ExceptionHandler;
import me.kubbidev.mumble.exception.ExceptionManager;
import me.kubbidev.mumble.loader.LinkApiLoader;
import me.kubbidev.mumble.jna.LinkApiHelper;
import me.kubbidev.mumble.jna.LinkApi;

@Environment(EnvType.CLIENT)
public final class MumbleLoader implements ClientTickEvents.EndTick {

    public static final String PLUGIN_NAME = "Minecraft";
    public static final String PLUGIN_LORE = "Minecraft (1.20.4)";
    public static final int PLUGIN_UI_VERSION = 2;

    private final MumblePos mumblePos;
    private LinkApi api;

    private ExceptionHandler.InitStatus result
            = ExceptionHandler.InitStatus.NOT_INITIALIZED;

    /**
     * Manages exception handling and status updates.
     */
    private final ExceptionManager exceptionManager;

    public MumbleLoader(MumbleLinkMod mod) {
        this.exceptionManager = new ExceptionManager(mod);
        try {
            // load the api
            this.api = LinkApiLoader.INSTANCE.load();
        } catch (Throwable t) {
            this.exceptionManager.handleException(t);
        }

        // Register the event to ensure the connection
        ClientPlayConnectionEvents.JOIN.register(
                (handler, sender, client) -> this.ensureMumbleConnected());

        // Initialize the mumble position defaults
        this.mumblePos = new MumblePos(this.api, this.exceptionManager);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (this.isMumbleConnected()) {
            if (client.player != null) {
                this.mumblePos.update(client.player);
                this.mumblePos.propagate();
            }
        }
    }

    public boolean isMumbleConnected() {
        return this.result == ExceptionHandler.InitStatus.LINKED;
    }

    private void ensureMumbleConnected() {
        this.result = initialize();
        this.exceptionManager.handleStatus(this.result);
    }

    private ExceptionHandler.@Nullable InitStatus initialize() {
        int i = this.api.initialize(
                LinkApiHelper.parseToCharBuffer(LinkApi.MAX_NAME_LENGTH, MumbleLoader.PLUGIN_NAME),
                LinkApiHelper.parseToCharBuffer(LinkApi.MAX_LORE_LENGTH, MumbleLoader.PLUGIN_LORE),
                MumbleLoader.PLUGIN_UI_VERSION
        );
        return ExceptionHandler.valueOf(ExceptionHandler.InitStatus.class, i);
    }
}
