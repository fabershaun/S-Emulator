package services;

import java.util.concurrent.*;

/**
* This service uses a single thread because only one periodic polling task is needed at a time.
* Having more threads would create redundant simultaneous polling tasks.
**/

public class ProgramPollingService {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> currentTask;


    // Starts polling every 2 seconds for the given task
    public void startPolling(Runnable task) {
        stopPolling(); // Cancel previous polling if exists

        currentTask = executor.scheduleAtFixedRate(
                task,
                0,    // No initial delay
                2,              // Every 2 seconds
                TimeUnit.SECONDS
        );
    }

    // Stops the active polling task
    public void stopPolling() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
