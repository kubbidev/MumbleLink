package me.kubbidev.mumble;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import me.kubbidev.mumble.api.Identity;
import me.kubbidev.mumble.exception.ExceptionHandler;
import me.kubbidev.mumble.jna.LinkApiHelper;
import me.kubbidev.mumble.jna.LinkApi;

public final class MumblePos {
    private final LinkApi api;
    private final ExceptionHandler exceptionHandler;

    private int uiTick = 0;
    private String identity; // [256]

    private float[] fAvatarPosition
            = {0, 0, 0}; // [3]
    private float[] fAvatarFront
            = {0, 0, 0}; // [3]
    private float[] fAvatarTop
            = {0, 0, 0}; // [3]

    private float[] fCameraPosition
            = {0, 0, 0}; // [3]
    private float[] fCameraFront
            = {0, 0, 0}; // [3]
    private float[] fCameraTop
            = {0, 0, 0}; // [3]

    public MumblePos(LinkApi api, ExceptionHandler exceptionHandler) {
        this.api = api;
        this.exceptionHandler = exceptionHandler;
    }

    public void propagate() {
        LinkApi.LinkedMem lm = new LinkApi.LinkedMem();
        lm.identity = LinkApiHelper.parseToCharBuffer(
                LinkApi.MAX_IDENTITY_LENGTH, this.identity).array();

        // [256]
        String name = MumbleLoader.PLUGIN_NAME;
        lm.name = LinkApiHelper.parseToCharBuffer(
                LinkApi.MAX_NAME_LENGTH, name).array();

        // [2048]
        String lore = MumbleLoader.PLUGIN_LORE;
        lm.lore = LinkApiHelper.parseToCharBuffer(
                LinkApi.MAX_LORE_LENGTH, lore).array();

        lm.uiTick = ++this.uiTick;
        lm.uiVersion = MumbleLoader.PLUGIN_UI_VERSION;

        lm.fAvatarPosition = this.fAvatarPosition;
        lm.fAvatarFront = this.fAvatarFront;
        lm.fAvatarTop = this.fAvatarTop;

        lm.fCameraPosition = this.fCameraPosition;
        lm.fCameraFront = this.fCameraFront;
        lm.fCameraTop = this.fCameraTop;

        var successMessage = this.api.updateData(lm);
        if (successMessage == 0) {
            this.exceptionHandler.handleStatus(
                    ExceptionHandler.UpdateStatus.NOT_INITIALIZED);
        }
    }

    public void update(@NotNull ClientPlayerEntity player, @NotNull MinecraftClient client) {
        this.identity = getIdentity(client);

        // 1 unit = 1 meter
        float fAvatarFrontX = 1;
        float fAvatarFrontY = 1;
        float fAvatarFrontZ = 1;

        float fCameraFrontX = 1;
        float fCameraFrontY = 1;
        float fCameraFrontZ = 1;

        float fAvatarTopX = 1;
        float fAvatarTopY = 1;
        float fAvatarTopZ = 1;

        float fCameraTopX = 1;
        float fCameraTopY = 1;
        float fCameraTopZ = 1;

        Vec3d rotationVector = player.getRotationVector();
        Vec3d oppositeRotationVector = player.getOppositeRotationVector(1.0F);
        Vec3d pos = player.getLerpedPos(1.0F);

        this.fAvatarPosition = new float[]{
                (float) pos.x,
                (float) pos.z,
                (float) pos.y
        };

        this.fAvatarFront = new float[]{
                (float) rotationVector.x * fAvatarFrontX,
                (float) rotationVector.z * fAvatarFrontZ,
                (float) rotationVector.y * fAvatarFrontY
        };

        this.fAvatarTop = new float[]{
                (float) oppositeRotationVector.x * fAvatarTopX,
                (float) oppositeRotationVector.z * fAvatarTopZ,
                (float) oppositeRotationVector.y * fAvatarTopY
        };

        this.fCameraPosition = new float[]{
                (float) pos.x,
                (float) pos.z,
                (float) pos.y
        };

        this.fCameraFront = new float[]{
                (float) rotationVector.x * fCameraFrontX,
                (float) rotationVector.z * fCameraFrontZ,
                (float) rotationVector.y * fCameraFrontY
        };

        this.fCameraTop = new float[]{
                (float) oppositeRotationVector.x * fCameraTopX,
                (float) oppositeRotationVector.z * fCameraTopZ,
                (float) oppositeRotationVector.y * fCameraTopY
        };
    }

    private String getIdentity(MinecraftClient client) {
        JsonObject identity = new JsonObject();

        var player = client.player;
        if (player != null) {
            var name = player.getDisplayName();
            if (name != null) identity.addProperty(Identity.NAME, name.getString());
        }

        var level = client.world;
        if (level != null) {
            JsonArray spawnCoordinates = new JsonArray();
            BlockPos spawnPos = level.getSpawnPos();
            spawnCoordinates.add(spawnPos.getX());
            spawnCoordinates.add(spawnPos.getY());
            spawnCoordinates.add(spawnPos.getZ());

            // append coordinates
            identity.add(Identity.WORLD_SPAWN, spawnCoordinates);

            // append the dimension
            identity.addProperty(Identity.WORLD, level.getRegistryKey().toString());
        }

        var string = identity.toString();
        if (string.length() > LinkApi.MAX_IDENTITY_LENGTH) {
            MumbleLinkMod.LOGGER.error("identity is too long '{}' (max. {}): '{}'",
                    string, LinkApi.MAX_IDENTITY_LENGTH, string.length());
        }
        return string;
    }
}
