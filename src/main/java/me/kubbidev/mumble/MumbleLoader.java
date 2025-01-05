package me.kubbidev.mumble;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import me.kubbidev.mumble.exception.ExceptionHandler;
import me.kubbidev.mumble.exception.ExceptionManager;
import me.kubbidev.mumble.loader.LinkApiLoader;
import me.kubbidev.mumble.jna.LinkApiHelper;
import me.kubbidev.mumble.jna.LinkApi;

import java.util.concurrent.atomic.AtomicBoolean;

public final class MumbleLoader implements ClientTickEvents.EndTick, Runnable {
    public static final String PLUGIN_NAME = "Minecraft";
    public static final String PLUGIN_LORE = "Minecraft (1.20.4)";
    public static final int PLUGIN_UI_VERSION = 2;

    private final MumbleLinkMod mod;
    private final MumblePos mumblePos;
    private LinkApi api;

    private ExceptionHandler.InitStatus result
            = ExceptionHandler.InitStatus.NOT_INITIALIZED;

    /**
     * Manages exception handling and status updates.
     */
    private final ExceptionManager exceptionManager;

    /**
     * Tracks the running state of the {@link MumbleLoader} thread.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public MumbleLoader(MumbleLinkMod mod) {
        this.exceptionManager = new ExceptionManager(mod);
        this.mod = mod;
        try {
            // load the api
            this.api = LinkApiLoader.INSTANCE.load("LinkAPI");
        } catch (Throwable t) {
            this.exceptionManager.handleException(t);
        }

        // Initialize the mumble position defaults
        this.mumblePos = new MumblePos(this.api, this.exceptionManager);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (client == null || this.isMumbleUnconnected()) {
            waitUntilMumbleConnected();
        } else {
            if (client.player != null) {
                this.mumblePos.update(client.player, client);
                this.mumblePos.propagate();
            }
        }
    }

    public boolean isMumbleUnconnected() {
        return this.result != ExceptionHandler.InitStatus.LINKED;
    }

    private void waitUntilMumbleConnected() {
        if (this.running.compareAndSet(false, true)) {
            try {
                this.mod.getScheduler().executeAsync(this);
            } catch (Throwable e) {
                this.running.set(false);
            }
        }
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (isMumbleUnconnected()) {
            this.result = initialize();
            this.exceptionManager.handleStatus(this.result);

            try {
                Thread.sleep(5000); // 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        this.running.set(false);
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
