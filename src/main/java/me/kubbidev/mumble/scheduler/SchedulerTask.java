package me.kubbidev.mumble.scheduler;

/**
 * Represents a scheduled task
 */
@FunctionalInterface
public interface SchedulerTask {

    /**
     * Cancels the task.
     */
    void cancel();
}
