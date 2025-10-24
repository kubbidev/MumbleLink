package me.kubbidev.mumble.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Used when you want to implement your own exception handling, instead of just printing the stack trace.
 */
@Environment(EnvType.CLIENT)
public interface ExceptionHandler {

    /**
     * Called when a exception was caught.
     *
     * @param e the thrown exception
     */
    void handleException(Throwable e);

    void handleStatus(InitStatus status);

    void handleStatus(UpdateStatus status);

    @Environment(EnvType.CLIENT)
    enum InitStatus {

        NOT_INITIALIZED,
        LINKED,

        NO_WIN_HANDLE,
        NO_WIN_STRUCTURE,

        NO_UNIX_HANDLE,
        NO_UNIX_STRUCTURE;

        public static InitStatus fromId(int id) {
            return switch (id) {
                case -1 -> NOT_INITIALIZED;
                case 0 -> LINKED;
                case 1 -> NO_WIN_HANDLE;
                case 2 -> NO_WIN_STRUCTURE;
                case 3 -> NO_UNIX_HANDLE;
                case 4 -> NO_UNIX_STRUCTURE;
                default -> throw new IllegalArgumentException("Unknown init status: " + id);
            };
        }

        public int id() {
            return ordinal() - 1;
        }
    }

    @Environment(EnvType.CLIENT)
    enum UpdateStatus {

        LINKED,
        NOT_INITIALIZED;

        public int id() {
            return ordinal();
        }
    }
}
