package com.alibaba.ttl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link TransmittableThreadLocal} can transmit value from the thread of submitting task to the thread of executing task.
 * <p>
 * Note: {@link TransmittableThreadLocal} extends {@link java.lang.InheritableThreadLocal},
 * so {@link TransmittableThreadLocal} first is a {@link java.lang.InheritableThreadLocal}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlRunnable
 * @see TtlCallable
 * @since 0.10.0
 */
public class TransmittableThreadLocal<T> extends InheritableThreadLocal<T> {
    private static final Logger logger = Logger.getLogger(TransmittableThreadLocal.class.getName());

    protected T copy(T parentValue) {
        return parentValue;
    }

    /**
     * Callback method before task object({@link TtlRunnable}/{@link TtlCallable}) execute.
     */
    protected void beforeExecute() {
    }

    /**
     * Callback method after task object({@link TtlRunnable}/{@link TtlCallable}) execute.
     */
    protected void afterExecute() {
    }

    //重写get和set方法确保变量备份在hold变量中
    @Override
    public final T get() {
        T value = super.get();
        if (null != value) {
            addValue();
        }
        return value;
    }

    @Override
    public final void set(T value) {
        super.set(value);
        if (null == value) {
            removeValue();
        } else {
            addValue();
        }
    }

    @Override
    public final void remove() {
        removeValue();
        super.remove();
    }

    private void superRemove() {
        super.remove();
    }

    private T superGet(){
        return super.get();
    }

    private T copyValue() {
        return copy(get());
    }

     /*
     1、此hold变量是TransmittableThreadLocal的静态 InheritableThreadLocal 变量，只初始化一次，所有TransmittableThreadLocal实例共享，
     借助重写的get和set方法，同一线程内所有的TransmittableThreadLocal变量都会保存到该线程内的holder变量中
     2、holder变量重写了InheritableThreadLocal的childValue(T parent)方法，默认实现是将parent直接返回，使父子线程的InheritableThreadLocal
     变量指向同一个对象，子线程对该对象的修改在父线程中也是可见的。重写后父子线程中InheritableThreadLocal是两个独立的对象，只是子线程的
     TransmittableThreadLocal变量初始化的时候取值和父线程一致。
     3、replay和restore方法在每次线程执行完任务后恢复至线程创建时的子线程的holder变量的状态
      */
    private static InheritableThreadLocal<Map<TransmittableThreadLocal<?>, ?>> holder =
            new InheritableThreadLocal<Map<TransmittableThreadLocal<?>, ?>>() {
                @Override
                protected Map<TransmittableThreadLocal<?>, ?> initialValue() {
                    return new WeakHashMap<TransmittableThreadLocal<?>, Object>();
                }

                @Override
                protected Map<TransmittableThreadLocal<?>, ?> childValue(Map<TransmittableThreadLocal<?>, ?> parentValue) {
                    return new WeakHashMap<TransmittableThreadLocal<?>, Object>(parentValue);
                }
            };

    private void addValue() {
        if (!holder.get().containsKey(this)) {
            holder.get().put(this, null); // WeakHashMap supports null value.
            System.out.println("addValue holder size-->"+holder.get().size()+",add value-->"+this.get());
        }
    }

    private void removeValue() {
        holder.get().remove(this);
        System.out.println("removeValue holder size-->"+holder.get().size());
    }

    private static void doExecuteCallback(boolean isBefore) {
        for (Map.Entry<TransmittableThreadLocal<?>, ?> entry : holder.get().entrySet()) {
            TransmittableThreadLocal<?> threadLocal = entry.getKey();

            try {
                if (isBefore) {
                    threadLocal.beforeExecute();
                } else {
                    threadLocal.afterExecute();
                }
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "TTL exception when " + (isBefore ? "beforeExecute" : "afterExecute") + ", cause: " + t.toString(), t);
                }
            }
        }
    }

    /**
     * Debug only method!
     */
    static void dump(String title) {
        if (title != null && title.length() > 0) {
            System.out.printf("Start TransmittableThreadLocal[%s] Dump...\n", title);
        } else {
            System.out.println("Start TransmittableThreadLocal Dump...");
        }

        for (Map.Entry<TransmittableThreadLocal<?>, ?> entry : holder.get().entrySet()) {
            final TransmittableThreadLocal<?> key = entry.getKey();
            System.out.println(key.get());
        }
        System.out.println("TransmittableThreadLocal Dump end!");
    }

    /**
     * Debug only method!
     */
    static void dump() {
        dump(null);
    }

    /**

     */
    public static class Transmitter {
        /**
         * 捕获父线程的所有TransmittableThreadLocal变量及其变量值，将父线程此时的TransmittableThreadLocal变量备份
         */
        public static Object capture() {
            System.out.println("capture hold size-->"+holder.get().size());
            Map<TransmittableThreadLocal<?>, Object> captured = new HashMap<TransmittableThreadLocal<?>, Object>();
            for (TransmittableThreadLocal<?> threadLocal : holder.get().keySet()) {
                captured.put(threadLocal, threadLocal.copyValue());
            }
            return captured;
        }

        /**
         * Replay the captured {@link TransmittableThreadLocal} values from {@link #capture()},
         * and return the backup {@link TransmittableThreadLocal} values in current thread before replay.
         *
         * 1、capturedMap中保存的TransmittableThreadLocal变量的取值在父子线程中不一定相同，但是TransmittableThreadLocal变量在Map中对应的value不变，
         * 即始终是capture()执行时父线程的TransmittableThreadLocal变量的取值。
         * 2、
         *
         */
        public static Object replay(Object captured) {
            @SuppressWarnings("unchecked")
            Map<TransmittableThreadLocal<?>, Object> capturedMap = (Map<TransmittableThreadLocal<?>, Object>) captured;
            Map<TransmittableThreadLocal<?>, Object> backup = new HashMap<TransmittableThreadLocal<?>, Object>();
            System.out.println("replay before holder size-->"+holder.get().size());
            try {
                for (Iterator<? extends Map.Entry<TransmittableThreadLocal<?>, ?>> iterator = holder.get().entrySet().iterator();
                     iterator.hasNext(); ) {
                    Map.Entry<TransmittableThreadLocal<?>, ?> next = iterator.next();
                    TransmittableThreadLocal<?> threadLocal = next.getKey();

                    // backup
                    backup.put(threadLocal, threadLocal.get());

                    //此处操作和setTtlValuesTo方法可以确保holder中保存的TransmittableThreadLocal变量和capturedMap一致
                    //即恢复子线程中的holder变量至capture()执行时主线程的holder变量的状态
                    if (!capturedMap.containsKey(threadLocal)) {
                        iterator.remove();
                        System.out.println("replay remove value-->"+threadLocal.superGet()+",holder size-->"+holder.get().size());
                        threadLocal.superRemove();
                    }
                }

                // set values to captured TTL
                setTtlValuesTo(capturedMap);

                // call beforeExecute callback
                doExecuteCallback(true);

                System.out.println("replay after holder size-->"+holder.get().size());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            return backup;
        }

        /**
         * Restore the backup {@link TransmittableThreadLocal} values from {@link Transmitter#replay(Object)}.
         *
         * @param backup the backup {@link TransmittableThreadLocal} values from {@link Transmitter#replay(Object)}
         * @since 2.3.0
         */
        public static void restore(Object backup) {
            @SuppressWarnings("unchecked")
            Map<TransmittableThreadLocal<?>, Object> backupMap = (Map<TransmittableThreadLocal<?>, Object>) backup;
            // call afterExecute callback
            doExecuteCallback(false);

            System.out.println("restore before holder size-->"+holder.get().size());

            for (Iterator<? extends Map.Entry<TransmittableThreadLocal<?>, ?>> iterator = holder.get().entrySet().iterator();
                 iterator.hasNext(); ) {
                Map.Entry<TransmittableThreadLocal<?>, ?> next = iterator.next();
                TransmittableThreadLocal<?> threadLocal = next.getKey();

                // 同replay，确保backUp中包含的TransmittableThreadLocal变量和holder中保持一致，即恢复线程的holder变量至此次job
                //开始运行的状态
                if (!backupMap.containsKey(threadLocal)) {
                    iterator.remove();
                    System.out.println("restore remove value-->"+threadLocal.superGet()+",holder size-->"+holder.get().size());
                    threadLocal.superRemove();
                }
            }

            // restore TTL values
            setTtlValuesTo(backupMap);
            System.out.println("restore after holder size-->"+holder.get().size());
        }

        private static void setTtlValuesTo(Map<TransmittableThreadLocal<?>, Object> ttlValues) {
            for (Map.Entry<TransmittableThreadLocal<?>, Object> entry : ttlValues.entrySet()) {
                @SuppressWarnings("unchecked")
                TransmittableThreadLocal<Object> threadLocal = (TransmittableThreadLocal<Object>) entry.getKey();
                /*
                   1、当传入的值是capturedMap时，entry.getValue()为父线程中此变量的取值，如果是backupMap则是子线程中次变量的取值
                   2、如果此时子线程的holder变量中不包含此变量，则通过set方法加入到holder中
                 */
                System.out.println("setTtlValuesTo before value-->"+threadLocal.get());
                threadLocal.set(entry.getValue());
                System.out.println("setTtlValuesTo after value-->"+threadLocal.get());
            }
        }

        /**
         * Util method for simplifying {@link #replay(Object)} and {@link #restore(Object)} operation.
         *
         */
        public static <R> R runSupplierWithCaptured(Object captured, Supplier<R> bizLogic) {
            Object backup = replay(captured);
            try {
                return bizLogic.get();
            } finally {
                restore(backup);
            }
        }

        /**
         * Util method for simplifying {@link #replay(Object)} and {@link #restore(Object)} operation.
         */
        public static <R> R runCallableWithCaptured(Object captured, Callable<R> bizLogic) throws Exception {
            Object backup = replay(captured);
            try {
                return bizLogic.call();
            } finally {
                restore(backup);
            }
        }

        private Transmitter() {
            throw new InstantiationError("Must not instantiate this class");
        }
    }
}
