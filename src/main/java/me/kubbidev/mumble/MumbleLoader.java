package me.kubbidev.mumble;

import me.kubbidev.mumble.api.Module;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import me.kubbidev.mumble.exception.ExceptionHandler;
import me.kubbidev.mumble.exception.ExceptionManager;
import me.kubbidev.mumble.loader.StructureLoader;
import me.kubbidev.mumble.jna.LinkApiHelper;
import me.kubbidev.mumble.jna.LinkApi;

@Environment(EnvType.CLIENT)
public class MumbleLoader implements ClientTickEvents.EndTick, Module {

    public static final String                      PLUGIN_NAME       = "Minecraft";
    public static final String                      PLUGIN_LORE       = "Minecraft (1.21.10)";
    public static final int                         PLUGIN_UI_VERSION = 2;
    // Initialize the mumble position defaults
    private final       MumblePos                   mumblePos         = new MumblePos(this);
    private             LinkApi                     api;
    private             ExceptionHandler.InitStatus result            = ExceptionHandler.InitStatus.NOT_INITIALIZED;
    /**
     * Manages exception handling and status updates.
     */
    private final       ExceptionManager            exceptionManager;

    public MumbleLoader(MumbleLinkMod mod) {
        exceptionManager = new ExceptionManager(mod);
        try {
            // unpack the api from resources
            api = StructureLoader.instantiateStructure(MumbleLinkConstants.LIBRARY_NAME);
        } catch (Throwable t) {
            exceptionManager.handleException(t);
        }
    }

    public LinkApi getApi() {
        return api;
    }

    public ExceptionManager getExceptionManager() {
        return exceptionManager;
    }

    @Override
    public void enable() {
        // Register the event to ensure the connection
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ensureMumbleConnected());
    }

    @Override
    public void disable() {
        // ignore
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (!isMumbleConnected()) {
            return;
        }
        if (client.player != null) {
            mumblePos.update(client.player);
            mumblePos.propagate();
        }
    }

    public boolean isMumbleConnected() {
        return result == ExceptionHandler.InitStatus.LINKED;
    }

    private void ensureMumbleConnected() {
        result = initialize();
        exceptionManager.handleStatus(result);
    }

    private ExceptionHandler.@Nullable InitStatus initialize() {
        int code = api.initialize(
            LinkApiHelper.parseToCharBuffer(LinkApi.MAX_NAME_LENGTH, MumbleLoader.PLUGIN_NAME),
            LinkApiHelper.parseToCharBuffer(LinkApi.MAX_LORE_LENGTH, MumbleLoader.PLUGIN_LORE),
            MumbleLoader.PLUGIN_UI_VERSION
        );
        return ExceptionHandler.valueOf(ExceptionHandler.InitStatus.class, code);
    }
}
