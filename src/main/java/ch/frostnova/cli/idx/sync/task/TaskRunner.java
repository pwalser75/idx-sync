package ch.frostnova.cli.idx.sync.task;

import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Thread.sleep;

/**
 * Task runner which executes tasks (non-concurrently) and reports the current progress to a progress monitor.
 * The task result can be positive (result value returned) or negative (exception rethrown).
 */
public class TaskRunner {

    private static final int MONITOR_INTERVAL_MS = 100;

    private final ProgressMonitor progressMonitor;

    /**
     * Create a task runner using the given progress monitor.
     *
     * @param progressMonitor progress monitor, required
     */
    public TaskRunner(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    /**
     * Run the given task
     *
     * @param task task, required
     * @param <R>  task result
     * @return task result, or rethrown exception if the task failed.
     */
    public <R> R run(Task<R> task) {
        progressMonitor.start(task.getName());
        var result = new ExecutionResult<R>();
        var runner = new Thread(() -> {
            try {
                var taskResult = task.run();
                synchronized (progressMonitor) {
                    progressMonitor.done("done");
                    result.done(taskResult);
                }
            } catch (Exception ex) {
                synchronized (progressMonitor) {
                    progressMonitor.done("failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    result.done(ex);
                }
            }
        });
        var monitor = new Thread(() -> {
            try {
                while (!result.isDone()) {
                    synchronized (progressMonitor) {
                        if (!result.isDone()) {
                            progressMonitor.update(max(0, min(1, task.getProgress())), task.getMessage());
                        }
                    }
                    sleep(MONITOR_INTERVAL_MS);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        runner.setDaemon(true);
        monitor.setDaemon(true);

        runner.start();
        monitor.start();
        try {
            runner.join();
            monitor.join();
            return result.get();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

    private static class ExecutionResult<T> {
        private T result;
        private Exception exception;
        private boolean done;

        public T get() throws Exception {
            if (!done) {
                throw new IllegalStateException("Not done yet");
            }
            if (exception != null) {
                throw exception;
            }
            return result;
        }

        public boolean isDone() {
            return done;
        }

        public void done(T result) {
            if (done) {
                throw new IllegalStateException("Already done");
            }
            this.done = true;
            this.result = result;
        }

        public void done(Exception exception) {
            if (done) {
                throw new IllegalStateException("Already done");
            }
            this.done = true;
            this.exception = exception;
        }
    }
}
