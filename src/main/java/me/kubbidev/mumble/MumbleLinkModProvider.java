package me.kubbidev.mumble;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
public final class MumbleLinkModProvider {
    private static MumbleLinkMod instance = null;

    public static MumbleLinkMod getInstance() {
        MumbleLinkMod instance = MumbleLinkModProvider.instance;
        if (instance == null) {
            throw new NotLoadedException();
        }
        return instance;
    }

    @ApiStatus.Internal
    static void register(MumbleLinkMod instance) {
        MumbleLinkModProvider.instance = instance;
    }

    @ApiStatus.Internal
    static void unregister() {
        MumbleLinkModProvider.instance = null;
    }

    @ApiStatus.Internal
    private MumbleLinkModProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }


    private static final class NotLoadedException extends IllegalStateException {
        private static final String MESSAGE = """
                The MumbleLink API isn't loaded yet!
                This could be because:
                  a) the MumbleLink mod is not installed or it failed to enable
                  b) the mod in the stacktrace does not declare a dependency on MumbleLink
                  c) the mod in the stacktrace is retrieving the API before the mod 'initialize' phase
                     (call the #get method in onInitialize, not the constructor!)
                """;

        NotLoadedException() {
            super(MESSAGE);
        }
    }

}
