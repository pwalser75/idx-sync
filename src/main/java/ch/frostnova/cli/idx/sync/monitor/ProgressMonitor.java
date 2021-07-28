package ch.frostnova.cli.idx.sync.monitor;

/**
 * A progress monitor that displays the progress of a task.
 */
public interface ProgressMonitor {

    /**
     * Start the progress with the given task name
     *
     * @param taskName name of the task, required
     */
    void start(String taskName);

    /**
     * Update the current progress.
     *
     * @param progress progress (0..1 = 0..100%)
     * @param message  message, optional
     */
    void update(double progress, String message);

    /**
     * Tells the monitor that the task is done (regardless of whether it was successful or not).
     *
     * @param message message to display (what was done, or error that occurred).
     */
    void done(String message);
}
