package ch.frostnova.cli.idx.sync.task;

import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;

public class TaskRunner {

    private final ProgressMonitor progressMonitor;

    public TaskRunner(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    public <R> R run(Task<R> task) {

        ExecutionResult<R> result = new ExecutionResult<>();
        Thread runner = new Thread(() -> {
            try {
                result.done(task.run());
                progressMonitor.done(task.getName(), "done");
            } catch (Exception ex) {
                result.done(ex);
                progressMonitor.done(task.getName(), "failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        });
        Thread monitor = new Thread(() -> {
            try {
                while (!result.isDone()) {
                    progressMonitor.update(task.getName(), task.getProgress(), task.getMessage());
                }
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
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
