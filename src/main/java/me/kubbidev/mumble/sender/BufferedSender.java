package me.kubbidev.mumble.sender;

import net.minecraft.text.Text;
import me.kubbidev.mumble.MumbleLinkMod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BufferedSender extends AbstractSender implements Runnable {
    private final List<Text> messageBuffer = new ArrayList<>();

    /**
     * Tracks the running state of the {@link BufferedSender} thread.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public BufferedSender(MumbleLinkMod mod) {
        super(mod);
    }

    @Override
    public void sendMessage(Text message) {
        synchronized (this.messageBuffer) {
            if (canSend()) {
                unsafeSendMessage(message);
            } else {
                this.messageBuffer.add(message);
                waitUntilRenderingPossible();
            }
        }
    }

    private void renderBuffer() {
        synchronized (this.messageBuffer) {
            this.messageBuffer.forEach(this::unsafeSendMessage);
            this.messageBuffer.clear();
        }
    }

    private void waitUntilRenderingPossible() {
        if (this.running.compareAndSet(false, true)) {
            try {
                this.mod.getScheduler().executeAsync(this);
            } catch (Throwable e) {
                this.running.set(false);
            }
        }
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (!canSend()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        renderBuffer();
        this.running.set(false);
    }
}