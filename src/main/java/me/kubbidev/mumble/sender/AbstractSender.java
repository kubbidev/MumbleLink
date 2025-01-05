package me.kubbidev.mumble.sender;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import me.kubbidev.mumble.MumbleLinkMod;

public abstract class AbstractSender implements Sender {
    protected MumbleLinkMod mod;

    public AbstractSender(MumbleLinkMod mod) {
        this.mod = mod;
    }

    @Override
    public void sendMessage(Text message) {
        if (canSend()) {
            unsafeSendMessage(message);
        }
    }

    protected boolean canSend() {
        return this.mod.getClient().map(client -> client.player != null).orElse(false);
    }

    protected void unsafeSendMessage(Text message) {
        this.mod.getClient().ifPresent(client -> {
            if (client.isOnThread()) {
                unsafeSendMessage(client, message);
            } else {
                // Send the message synchronously if not on the main thread
                client.executeSync(() -> unsafeSendMessage(client, message));
            }
        });
    }

    private void unsafeSendMessage(MinecraftClient client, Text message) {
        if (client.player != null) {
            client.player.sendMessage(message);
        }
    }
}
