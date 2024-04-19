package ch.frostnova.cli.idx.sync.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A task consisting of other tasks.
 */
public class BatchTask<R> implements Task<List<R>> {

    private final String name;
    private final List<Task<R>> tasks;
    private int index;
    private Task<R> current;
    private List<R> results = new ArrayList<>();

    public BatchTask(String name, List<Task<R>> tasks) {
        this.name = requireNonNull(name, "name is required");
        this.tasks = requireNonNull(tasks, "tasks is required");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<R> run() throws Exception {
        for (var task : tasks) {
            current = task;
            results.add(current.run());
            index++;
        }
        return results;
    }

    @Override
    public double getProgress() {
        if (current == null) {
            return 0;
        }
        return (index + current.getProgress()) / tasks.size();
    }

    @Override
    public String getMessage() {
        return Optional.ofNullable(current).map(Task::getMessage).orElse("");
    }
}
