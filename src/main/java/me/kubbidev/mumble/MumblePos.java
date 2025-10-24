package me.kubbidev.mumble;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.kubbidev.mumble.api.Key;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import me.kubbidev.mumble.exception.ExceptionHandler;
import me.kubbidev.mumble.jna.LinkApiHelper;
import me.kubbidev.mumble.jna.LinkApi;
import net.minecraft.world.WorldProperties.SpawnPoint;

@Environment(EnvType.CLIENT)
public class MumblePos {

    private final MumbleLoader loader;
    private       int          uiTick          = 0;
    private       String       identity; // [256]
    private       String       context; // [256]
    private       float[]      fAvatarFront    = {0, 0, 0}; // [3]
    private       float[]      fCameraFront    = {0, 0, 0}; // [3]
    private       float[]      fAvatarPosition = {0, 0, 0}; // [3]
    private       float[]      fCameraPosition = {0, 0, 0}; // [3]
    private       float[]      fAvatarTop      = {0, 0, 0}; // [3]
    private       float[]      fCameraTop      = {0, 0, 0}; // [3]

    public MumblePos(MumbleLoader loader) {
        this.loader = loader;
    }

    public void update(ClientPlayerEntity clientPlayer) {
        identity = getIdentity(clientPlayer);
        context = getContext();

        Vec3d rotationVector = clientPlayer.getRotationVector();
        Vec3d oppositeRotationVector = clientPlayer.getOppositeRotationVector(1.0F);
        Vec3d pos = clientPlayer.getEntityPos();

        fAvatarFront = new float[]{
            (float) rotationVector.x,
            (float) rotationVector.z,
            (float) rotationVector.y
        };
        fCameraFront = new float[]{
            (float) rotationVector.x,
            (float) rotationVector.z,
            (float) rotationVector.y
        };

        fAvatarPosition = new float[]{
            (float) pos.x,
            (float) pos.z,
            (float) pos.y
        };
        fCameraPosition = new float[]{
            (float) pos.x,
            (float) pos.z,
            (float) pos.y
        };

        fAvatarTop = new float[]{
            (float) oppositeRotationVector.x,
            (float) oppositeRotationVector.z,
            (float) oppositeRotationVector.y
        };
        fCameraTop = new float[]{
            (float) oppositeRotationVector.x,
            (float) oppositeRotationVector.z,
            (float) oppositeRotationVector.y
        };
    }

    private String getIdentity(ClientPlayerEntity clientPlayer) {
        JsonObject identity = new JsonObject();

        Text name = clientPlayer.getDisplayName();
        if (name != null) {
            identity.addProperty(Key.Identity.NAME, name.getString());
        }

        JsonArray spawnCoordinates = new JsonArray();
        SpawnPoint spawnPoint = clientPlayer.getEntityWorld().getSpawnPoint();

        BlockPos spawnPos = spawnPoint.getPos();
        spawnCoordinates.add(spawnPos.getX());
        spawnCoordinates.add(spawnPos.getY());
        spawnCoordinates.add(spawnPos.getZ());

        // Append coordinates
        identity.add(Key.Identity.WORLD_SPAWN, spawnCoordinates);

        // Append the dimension
        RegistryKey<World> dimensionKey = clientPlayer.getEntityWorld().getRegistryKey();
        identity.addProperty(Key.Identity.DIMENSION, dimensionKey.toString());

        String string = identity.toString();
        if (string.length() > LinkApi.MAX_IDENTITY_LENGTH) {
            MumbleLinkMod.LOGGER.error("Identity is too long '{}' (max. {}): '{}'",
                string,
                LinkApi.MAX_IDENTITY_LENGTH, string.length());
        }
        return string;
    }

    private String getContext() {
        JsonObject context = new JsonObject();
        context.addProperty(Key.Context.DOMAIN, MumbleLinkConstants.MUMBLE_CONTEXT_DOMAIN);

        String string = context.toString();
        if (string.length() > LinkApi.MAX_CONTEXT_LENGTH) {
            MumbleLinkMod.LOGGER.error("Context is too long '{}' (max. {}): '{}'",
                string,
                LinkApi.MAX_CONTEXT_LENGTH, string.length());
        }
        return context.toString();
    }

    public void propagate() {
        LinkApi.LinkedMem lm = new LinkApi.LinkedMem();

        // [256]
        lm.identity = LinkApiHelper.parseToCharBuffer(
            LinkApi.MAX_IDENTITY_LENGTH, identity).array();

        // [256]
        lm.context = LinkApiHelper.parseToByteBuffer(LinkApi.MAX_IDENTITY_LENGTH, context).array();
        lm.context_len = context.length();

        // [256]
        String name = MumbleLoader.PLUGIN_NAME;
        lm.name = LinkApiHelper.parseToCharBuffer(LinkApi.MAX_NAME_LENGTH, name).array();

        // [2048]
        String lore = MumbleLoader.PLUGIN_LORE;
        lm.description = LinkApiHelper.parseToCharBuffer(LinkApi.MAX_LORE_LENGTH, lore).array();

        lm.uiTick = ++uiTick;
        lm.uiVersion = MumbleLoader.PLUGIN_UI_VERSION;

        lm.fAvatarFront = fAvatarFront;
        lm.fCameraFront = fCameraFront;

        lm.fAvatarPosition = fAvatarPosition;
        lm.fCameraPosition = fCameraPosition;

        lm.fAvatarTop = fAvatarTop;
        lm.fCameraTop = fCameraTop;

        LinkApi api = loader.getApi();
        byte successMessage = api.updateData(lm);
        if (successMessage == 0) {
            loader.getExceptionManager().handleStatus(ExceptionHandler.UpdateStatus.NOT_INITIALIZED);
        }
    }
}
