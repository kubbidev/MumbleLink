package me.kubbidev.mumble.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.kubbidev.mumble.MumbleLinkMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ExceptionManager implements ExceptionHandler {

    private final MinecraftClient client;

    public ExceptionManager(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void handleException(Throwable e) {
        throw new IllegalStateException("Unexpected exception", e);
    }

    @Override
    public void handleStatus(InitStatus status) {
        MumbleLinkMod.LOGGER.info("Init status: {} ({})", status.id(), status);

        if (status == InitStatus.LINKED) {
            ToastManager toastManager = client.getToastManager();

            SystemToast.add(toastManager, SystemToast.Type.PERIODIC_NOTIFICATION,
                Text.literal(MumbleLinkMod.MOD_CONTAINER.getMetadata().getName()),
                Text.translatable("feature.mumblelink.status.toast.linked"));
        }
    }

    @Override
    public void handleStatus(UpdateStatus status) {
        if (status != UpdateStatus.LINKED) {
            MumbleLinkMod.LOGGER.error("Update failed! Status: {} ({})", status.id(), status);
        }
    }
}
