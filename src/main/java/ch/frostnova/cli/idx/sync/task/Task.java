package ch.frostnova.cli.idx.sync.task;

/**
 * Definition of a named Task with observable progress and message.
 * The task is supposed to be executed asynchronously and be observed during execution,
 * polling its progress and message at certain intervals.
 *
 * @param <R> result type
 */
public interface Task<R> {

    /**
     * Returns the name of the task.
     *
     * @return name, required
     */
    String getName();

    /**
     * Executes the task. The task is considered running once this method is called until it terminates.x
     *
     * @return R task execution result.
     * @throws Exception any exception
     */
    R run() throws Exception;

    /**
     * Returns the current progress (0..1 = 0..100%).
     *
     * @return current progress
     */
    double getProgress();

    /**
     * Returns an (optional) message telling what the task is currently doing.
     *
     * @return message, optional
     */
    String getMessage();
}
