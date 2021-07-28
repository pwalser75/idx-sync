package ch.frostnova.cli.idx.sync.monitor;

/**
 * A progress monitor that displays the progress of a task.
 */
public interface ProgressMonitor {

    /**
     * Update the current progress.
     *
     * @param taskName name of the task, required
     * @param progress progress (0..1 = 0..100%)
     * @param message  message, optional
     */
    void update(String taskName, double progress, String message);

    /**
     * Tells the monitor that the task is done (regardless of whether it was successful or not).
     *
     * @param taskName name of the task, required
     * @param message  message to display (what was done, or error that occurred).
     */
    void done(String taskName, String message);
}
