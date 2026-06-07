package hellfirepvp.modularmachinery.port.perf;

import java.util.concurrent.atomic.AtomicLong;

public final class MmcePerformanceStats {
    private static final AtomicLong EXECUTED_COUNT = new AtomicLong();
    private static final AtomicLong TOTAL_EXECUTED = new AtomicLong();
    private static final AtomicLong TOTAL_USED_TIME_MICROS = new AtomicLong();
    private static final AtomicLong TASK_USED_TIME_MICROS = new AtomicLong();

    public static void recordRecipeRun(long elapsedNanos, int parallelism, boolean active) {
        if (!active) {
            return;
        }
        long elapsedMicros = Math.max(0L, elapsedNanos / 1_000L);
        long tasks = Math.max(1L, parallelism);
        EXECUTED_COUNT.incrementAndGet();
        TOTAL_EXECUTED.addAndGet(tasks);
        TOTAL_USED_TIME_MICROS.addAndGet(elapsedMicros);
        TASK_USED_TIME_MICROS.addAndGet(elapsedMicros * tasks);
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                EXECUTED_COUNT.get(),
                TOTAL_EXECUTED.get(),
                TOTAL_USED_TIME_MICROS.get(),
                TASK_USED_TIME_MICROS.get()
        );
    }

    public static void reset() {
        EXECUTED_COUNT.set(0L);
        TOTAL_EXECUTED.set(0L);
        TOTAL_USED_TIME_MICROS.set(0L);
        TASK_USED_TIME_MICROS.set(0L);
    }

    public record Snapshot(
            long executedCount,
            long totalExecuted,
            long totalUsedTimeMicros,
            long taskUsedTimeMicros
    ) {
    }

    private MmcePerformanceStats() {
    }
}
