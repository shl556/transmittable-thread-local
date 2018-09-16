package com.alibaba.ttl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * {@link TtlRunnable} decorate {@link Runnable}, so as to get {@link TransmittableThreadLocal}
 * and transmit it to the time of {@link Runnable} execution, needed when use {@link Runnable} to thread pool.
 * <p>
 * Use factory methods {@link #get} / {@link #gets} to create instance.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl.threadpool.TtlExecutors
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 0.9.0
 */
public final class TtlRunnable implements Runnable {
    private final AtomicReference<Object> capturedRef;
    private final Runnable runnable;
    private final boolean releaseTtlValueReferenceAfterRun;

    //各种静态包装方法最终都会走到此构造方法
    private TtlRunnable(Runnable runnable, boolean releaseTtlValueReferenceAfterRun) {
        /*
         1、capture()方法返回的对象类型是Map<TransmittableThreadLocal<?>, Object>，该方法在父线程中执行，
        返回值是此时父线程包含的TransmittableThreadLocal变量及其在父线程中的取值。
         2、当子线程执行TtlRunnable的run方法时，capturedRef也会随着TtlRunnable实例传入子线程中，子线程capturedRef变量中包含的
         TransmittableThreadLocal变量继承自父线程，线程复用的情形下则跟上一次该子线程执行任务完成后的变量状态一致。
         */
        this.capturedRef = new AtomicReference<Object>(capture());
        this.runnable = runnable;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * wrap method {@link Runnable#run()}.
     */
    @Override
    public void run() {
        /*
          1、run方法在子线程中执行
          2、replay方法将capturedRef中保存的来自父线程中TransmittableThreadLocal变量写入到子线程对应的变量中，保证子线程每次执行时
          TransmittableThreadLocal变量的初始值一致。该方法返回子线程此时包含的TransmittableThreadLocal变量。
          3、restore方法用于还原子线程中包含的TransmittableThreadLocal变量至run方法开始执行的状态
         */
        Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        Object backup = replay(captured);
        try {
            runnable.run();
        } finally {
            restore(backup);
        }
    }

    /**
     * return original/unwrapped {@link Runnable}.
     */
    public Runnable getRunnable() {
        return runnable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TtlRunnable that = (TtlRunnable) o;

        return runnable.equals(that.runnable);
    }

    @Override
    public int hashCode() {
        return runnable.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + runnable.toString();
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    public static TtlRunnable get(Runnable runnable) {
        return get(runnable, false, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable                         input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    public static TtlRunnable get(Runnable runnable, boolean releaseTtlValueReferenceAfterRun) {
        return get(runnable, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable                         input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's {@link TtlRunnable},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException when input is {@link TtlRunnable} already and not idempotent.
     */
    public static TtlRunnable get(Runnable runnable, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == runnable) {
            return null;
        }

        if (runnable instanceof TtlRunnable) {
            if (idempotent) {
                // avoid redundant decoration, and ensure idempotency
                return (TtlRunnable) runnable;
            } else {
                throw new IllegalStateException("Already TtlRunnable!");
            }
        }
        return new TtlRunnable(runnable, releaseTtlValueReferenceAfterRun);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    public static List<TtlRunnable> gets(Collection<? extends Runnable> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    public static List<TtlRunnable> gets(Collection<? extends Runnable> tasks, boolean releaseTtlValueReferenceAfterRun) {
        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's {@link TtlRunnable},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlRunnable} already and not idempotent.
     */
    public static List<TtlRunnable> gets(Collection<? extends Runnable> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == tasks) {
            return Collections.emptyList();
        }
        List<TtlRunnable> copy = new ArrayList<TtlRunnable>();
        for (Runnable task : tasks) {
            copy.add(TtlRunnable.get(task, releaseTtlValueReferenceAfterRun, idempotent));
        }
        return copy;
    }
}
