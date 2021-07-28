package ch.frostnova.cli.idx.sync.util;

public final class Invocation {

    private Invocation() {

    }

    public static void runUnchecked(CheckedRunnable runnable) {
        try {
            runnable.runUnchecked();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

    public static <T> T runUnchecked(CheckedSupplier<T> supplier) {
        try {
            return supplier.supplyUnchecked();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

    public interface CheckedRunnable {

        /**
         * Functional contract
         *
         * @throws Exception optional exception
         */
        void run() throws Throwable;

        /**
         * Unchecked execution: execute checked and rethrow any exception as {@link RuntimeException}.
         */
        default void runUnchecked() {
            try {
                run();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Error error) {
                throw (Error) error;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public interface CheckedSupplier<T> {

        /**
         * Functional contract
         *
         * @return return value
         * @throws Exception optional exception
         */
        T supply() throws Throwable;

        /**
         * Unchecked execution: execute checked and rethrow any exception as {@link RuntimeException}.
         */
        default T supplyUnchecked() {
            try {
                return supply();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Error error) {
                throw (Error) error;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
