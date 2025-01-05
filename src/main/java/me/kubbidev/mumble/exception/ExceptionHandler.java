package me.kubbidev.mumble.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.EnumSet;

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

    @FunctionalInterface
    interface Identifiable {
        int getId();
    }

    static <E extends Enum<E> & Identifiable> E valueOf(Class<E> type, int id) {
        return EnumSet.allOf(type).stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    @Environment(EnvType.CLIENT)
    enum InitStatus implements Identifiable {

        NOT_INITIALIZED(-1),
        LINKED(0),

        NO_WIN_HANDLE(1),
        NO_WIN_STRUCTURE(2),

        NO_UNIX_HANDLE(3),
        NO_UNIX_STRUCTURE(4);

        private final int id;

        InitStatus(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return this.id;
        }
    }

    @Environment(EnvType.CLIENT)
    enum UpdateStatus implements Identifiable {

        LINKED(0),
        NOT_INITIALIZED(1);

        private final int id;

        UpdateStatus(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return this.id;
        }
    }
}
