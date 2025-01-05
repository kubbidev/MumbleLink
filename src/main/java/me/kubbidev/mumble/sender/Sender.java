package me.kubbidev.mumble.sender;

import net.minecraft.text.Text;

public interface Sender {
    String LINK_SUCCESS_MESSAGE = "Mumble linked.";

    void sendMessage(Text message);
}
