package ch.frostnova.cli.idx.sync;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Concurrent {

    private IntFunction<Runnable> createTask = i -> () -> System.out.printf("Task #%d on Thread %s\n", i, Thread.currentThread().getName());

    private List<Runnable> tasks = IntStream.range(0, 100).mapToObj(createTask).collect(Collectors.toList());

    @Test
    void executeWithThread() {


    }

    @Test
    void executeWithForkJoin() {

        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        // forkJoinPool.submit(tasks)

        new RecursiveAction() {
            @Override
            protected void compute() {

            }
        };


    }

    @Test
    void executeWithStream() {
        tasks.stream().parallel().forEach(Runnable::run);
    }

    @Test
    void executeWithCompleteableFututre() {
        tasks.forEach(task -> CompletableFuture.runAsync(task));
    }
}
