package me.kubbidev.mumble;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.kubbidev.mumble.api.Key;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import me.kubbidev.mumble.exception.ExceptionHandler;
import me.kubbidev.mumble.jna.LinkApiHelper;
import me.kubbidev.mumble.jna.LinkApi;

@Environment(EnvType.CLIENT)
public final class MumblePos {
    private final LinkApi api;
    private final ExceptionHandler exceptionHandler;

    private int uiTick = 0;
    private String identity; // [256]
    private String context; // [256]

    private float[] fAvatarFront
            = {0, 0, 0}; // [3]
    private float[] fCameraFront
            = {0, 0, 0}; // [3]

    private float[] fAvatarPosition
            = {0, 0, 0}; // [3]
    private float[] fCameraPosition
            = {0, 0, 0}; // [3]

    private float[] fAvatarTop
            = {0, 0, 0}; // [3]
    private float[] fCameraTop
            = {0, 0, 0}; // [3]

    public MumblePos(LinkApi api, ExceptionHandler exceptionHandler) {
        this.api = api;
        this.exceptionHandler = exceptionHandler;
    }

    public void propagate() {
        LinkApi.LinkedMem lm = new LinkApi.LinkedMem();

        // [256]
        lm.identity = LinkApiHelper.parseToCharBuffer(
                LinkApi.MAX_IDENTITY_LENGTH, this.identity).array();

        // [256]
        lm.context = LinkApiHelper.parseToByteBuffer(
                LinkApi.MAX_IDENTITY_LENGTH, this.context).array();

        lm.context_len = this.context.length();

        // [256]
        String name = MumbleLoader.PLUGIN_NAME;
        lm.name = LinkApiHelper.parseToCharBuffer(
                LinkApi.MAX_NAME_LENGTH, name).array();

        // [2048]
        String lore = MumbleLoader.PLUGIN_LORE;
        lm.description = LinkApiHelper.parseToCharBuffer(
                LinkApi.MAX_LORE_LENGTH, lore).array();

        lm.uiTick = ++this.uiTick;
        lm.uiVersion = MumbleLoader.PLUGIN_UI_VERSION;

        lm.fAvatarFront = this.fAvatarFront;
        lm.fCameraFront = this.fCameraFront;

        lm.fAvatarPosition = this.fAvatarPosition;
        lm.fCameraPosition = this.fCameraPosition;

        lm.fAvatarTop = this.fAvatarTop;
        lm.fCameraTop = this.fCameraTop;

        var successMessage = this.api.updateData(lm);
        if (successMessage == 0) {
            this.exceptionHandler.handleStatus(
                    ExceptionHandler.UpdateStatus.NOT_INITIALIZED);
        }
    }

    public void update(@NotNull ClientPlayerEntity player) {
        this.identity = getIdentity(player);
        this.context = getContext();

        Vec3d rotationVector = player.getRotationVector();
        Vec3d oppositeRotationVector = player.getOppositeRotationVector(1.0F);
        Vec3d pos = player.getPos();

        this.fAvatarFront = new float[]{
                (float) rotationVector.x,
                (float) rotationVector.z,
                (float) rotationVector.y
        };
        this.fCameraFront = new float[]{
                (float) rotationVector.x,
                (float) rotationVector.z,
                (float) rotationVector.y
        };

        this.fAvatarPosition = new float[]{
                (float) pos.x,
                (float) pos.z,
                (float) pos.y
        };
        this.fCameraPosition = new float[]{
                (float) pos.x,
                (float) pos.z,
                (float) pos.y
        };

        this.fAvatarTop = new float[]{
                (float) oppositeRotationVector.x,
                (float) oppositeRotationVector.z,
                (float) oppositeRotationVector.y
        };
        this.fCameraTop = new float[]{
                (float) oppositeRotationVector.x,
                (float) oppositeRotationVector.z,
                (float) oppositeRotationVector.y
        };
    }

    private String getIdentity(ClientPlayerEntity player) {
        JsonObject identity = new JsonObject();

        var name = player.getDisplayName();
        if (name != null) identity.addProperty(Key.Identity.NAME, name.getString());

        JsonArray spawnCoordinates = new JsonArray();
        BlockPos spawnPos = player.clientWorld.getSpawnPos();
        spawnCoordinates.add(spawnPos.getX());
        spawnCoordinates.add(spawnPos.getY());
        spawnCoordinates.add(spawnPos.getZ());

        // append coordinates
        identity.add(Key.Identity.WORLD_SPAWN, spawnCoordinates);

        // append the dimension
        RegistryKey<World> dimensionKey = player.clientWorld.getRegistryKey();
        identity.addProperty(Key.Identity.DIMENSION, dimensionKey.toString());


        var string = identity.toString();
        if (string.length() > LinkApi.MAX_IDENTITY_LENGTH) {
            MumbleLinkMod.LOGGER.error("Identity is too long '{}' (max. {}): '{}'",
                    string, LinkApi.MAX_IDENTITY_LENGTH, string.length());
        }
        return string;
    }

    private String getContext() {
        JsonObject context = new JsonObject();
        context.addProperty(Key.Context.DOMAIN, MumbleLinkConstants.MUMBLE_CONTEXT_DOMAIN);

        var string = context.toString();
        if (string.length() > LinkApi.MAX_CONTEXT_LENGTH) {
            MumbleLinkMod.LOGGER.error("Context is too long '{}' (max. {}): '{}'",
                    string, LinkApi.MAX_CONTEXT_LENGTH, string.length());
        }
        return context.toString();
    }
}
