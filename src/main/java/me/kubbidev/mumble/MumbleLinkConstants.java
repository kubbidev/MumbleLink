package me.kubbidev.mumble;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MumbleLinkConstants {
    private MumbleLinkConstants() {
    }

    public static final String MUMBLE_CONTEXT_DOMAIN = "AllTalk";
}
