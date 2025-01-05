package me.kubbidev.mumble.exception;

import net.minecraft.text.Text;
import me.kubbidev.mumble.MumbleLinkMod;
import me.kubbidev.mumble.sender.BufferedSender;
import me.kubbidev.mumble.sender.Sender;

public class ExceptionManager implements ExceptionHandler {
    private final Sender sender;

    public ExceptionManager(MumbleLinkMod mod) {
        this.sender = new BufferedSender(mod);
    }

    @Override
    public void handleException(Throwable e) {
        throw new IllegalStateException("Unexpected exception", e);
    }

    @Override
    public void handleStatus(InitStatus status) {
        if (status == InitStatus.LINKED) {
            this.sender.sendMessage(Text.literal("Mumble linked."));
        }
    }

    @Override
    public void handleStatus(UpdateStatus status) {
        if (status != UpdateStatus.LINKED) {
            MumbleLinkMod.LOGGER.error("Update failed! Status: {} ({})", status.getId(), status);
        }
    }
}
