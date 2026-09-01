package me.kubbidev.mumble.exception;

import me.kubbidev.mumble.MumbleLinkMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ExceptionManager implements ExceptionHandler {

    private final Minecraft client;

    public ExceptionManager(Minecraft client) {
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
            ToastManager toastManager = client.gui.toastManager();

            SystemToast.add(toastManager, SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal(MumbleLinkMod.MOD_CONTAINER.getMetadata().getName()),
                Component.translatable("feature.mumblelink.status.toast.linked"));
        }
    }

    @Override
    public void handleStatus(UpdateStatus status) {
        if (status != UpdateStatus.LINKED) {
            MumbleLinkMod.LOGGER.error("Update failed! Status: {} ({})", status.id(), status);
        }
    }
}
