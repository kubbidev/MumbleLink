package me.kubbidev.mumble;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class MumbleLinkMod implements ClientModInitializer {

    public static final String MOD_ID = "mumblelink";

    /**
     * The mod container singleton instance.
     */
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID)
        .orElseThrow(() -> new RuntimeException("Could not get the MumbleLink mod container."));

    /**
     * The universal mod logger
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * The Minecraft client instance
     */
    private @Nullable Minecraft    client;
    // init during enable
    private @Nullable MumbleTicker mumbleTicker;

    // lifecycle

    @Override
    public void onInitializeClient() {
        // Register the Client startup/shutdown events now
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
    }

    private void onClientStarted(Minecraft client) {
        this.client = client;
        Instant startTime = Instant.now();

        // enable the mumble loader events
        MumbleLoader mumbleLoader = new MumbleLoader(client);
        mumbleLoader.setup();

        // enable the mumble position ticker
        mumbleTicker = new MumbleTicker(mumbleLoader);
        mumbleTicker.enable();

        // successfully print the time taken when loading the mod!
        Duration timeTaken = Duration.between(startTime, Instant.now());
        LOGGER.info("Successfully enabled. (took {}ms)", timeTaken.toMillis());
    }

    private void onClientStopping(Minecraft client) {
        LOGGER.info("Starting shutdown process...");

        // disable ticking
        if (mumbleTicker != null) {
            mumbleTicker.disable();
        }

        this.client = null;
        LOGGER.info("Goodbye!");
    }

    // Minecraft singleton getter

    public Optional<Minecraft> getClient() {
        return Optional.ofNullable(client);
    }
}
