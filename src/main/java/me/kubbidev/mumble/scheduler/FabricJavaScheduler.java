package me.kubbidev.mumble.scheduler;

import me.kubbidev.mumble.MumbleLinkMod;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SchedulerAdapter} using a {@link ScheduledExecutorService}.
 */
public class FabricJavaScheduler implements SchedulerAdapter {
    private static final int PARALLELISM = 16;
    private final Executor sync;

    private final ScheduledThreadPoolExecutor scheduler;
    private final ForkJoinPool worker;

    @SuppressWarnings("resource")
    public FabricJavaScheduler(MumbleLinkMod mod) {
        this.sync = r -> mod.getClient().orElseThrow(
                () -> new IllegalStateException("Client not ready")).submitAndJoin(r);

        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName("mumblelink-scheduler");
            return thread;
        });
        this.scheduler.setRemoveOnCancelPolicy(true);
        this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.worker = new ForkJoinPool(PARALLELISM, new WorkerThreadFactory(), new ExceptionHandler(), false);
    }

    @Override
    public Executor async() {
        return this.worker;
    }

    @Override
    public Executor sync() {
        return this.sync;
    }

    @Override
    public SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = this.scheduler.schedule(() -> this.worker.execute(task), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        ScheduledFuture<?> future = this.scheduler.scheduleAtFixedRate(() -> this.worker.execute(task), interval, interval, unit);
        return () -> future.cancel(false);
    }

    @Override
    public void shutdownScheduler() {
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                MumbleLinkMod.LOGGER.error("Timed out waiting for the MumbleLink scheduler to terminate");
                reportRunningTasks(thread -> thread.getName().equals("mumblelink-scheduler"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownExecutor() {
        this.worker.shutdown();
        try {
            if (!this.worker.awaitTermination(1, TimeUnit.MINUTES)) {
                MumbleLinkMod.LOGGER.error("Timed out waiting for the MumbleLink worker thread pool to terminate");
                reportRunningTasks(thread -> thread.getName().startsWith("mumblelink-worker-"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void reportRunningTasks(Predicate<Thread> predicate) {
        Thread.getAllStackTraces().forEach((thread, stack) -> {
            if (predicate.test(thread)) {
                MumbleLinkMod.LOGGER.warn("Thread {} is blocked, and may be the reason for the slow shutdown!\n{}",
                        thread.getName(), Arrays.stream(stack).map(el -> "  " + el).collect(Collectors.joining("\n")));
            }
        });
    }

    private static final class WorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private static final AtomicInteger COUNT = new AtomicInteger(0);

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setDaemon(true);
            thread.setName("mumblelink-worker-" + COUNT.getAndIncrement());
            return thread;
        }
    }

    private static final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            MumbleLinkMod.LOGGER.warn("Thread {} threw an uncaught exception", t.getName(), e);
        }
    }
}
