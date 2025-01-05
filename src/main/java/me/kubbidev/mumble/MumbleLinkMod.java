package me.kubbidev.mumble;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.kubbidev.mumble.scheduler.FabricJavaScheduler;
import me.kubbidev.mumble.scheduler.SchedulerAdapter;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

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
     * A scheduler adapter for the platform
     */
    private final SchedulerAdapter schedulerAdapter;

    /**
     * The time when the mod was enabled
     */
    private Instant startTime;

    /**
     * The Minecraft client instance
     */
    private MinecraftClient client;

    // init during enable
    private MumbleTicker mumbleTicker;

    public MumbleLinkMod() {
        this.schedulerAdapter = new FabricJavaScheduler(this);
    }

    // provide adapters

    public SchedulerAdapter getScheduler() {
        return this.schedulerAdapter;
    }

    public Instant getStartTime() {
        return this.startTime;
    }

    // lifecycle

    @Override
    public void onInitializeClient() {
        // Register the Client startup/shutdown events now
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
    }

    private void onClientStarted(MinecraftClient client) {
        this.startTime = Instant.now();

        // register the mumble link provider
        MumbleLinkModProvider.register(this);

        // enable the mumble position ticker
        this.mumbleTicker = new MumbleTicker(this);
        this.mumbleTicker.enable();

        // successfully print the time taken when loading the mod!
        Duration timeTaken = Duration.between(this.getStartTime(), Instant.now());
        LOGGER.info("Successfully enabled. (took {}ms)", timeTaken.toMillis());
    }

    private void onClientStopping(MinecraftClient client) {
        LOGGER.info("Starting shutdown process...");

        // cancel delayed/repeating tasks
        getScheduler().shutdownScheduler();

        // disable ticking
        this.mumbleTicker.disable();

        // unregister provider
        MumbleLinkModProvider.unregister();

        // shutdown async executor pool
        getScheduler().shutdownExecutor();

        this.client = null;
        LOGGER.info("Goodbye!");
    }

    // MinecraftClient singleton getter

    public Optional<MinecraftClient> getClient() {
        return Optional.ofNullable(this.client);
    }
}
