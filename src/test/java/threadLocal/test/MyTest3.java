package threadLocal.test;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRecursiveTask;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyTest3 {

    @Test
    public void name() throws Exception{
        ThreadLocal<Map<ThreadLocal<String>,String>> threadLocal=new ThreadLocal<Map<ThreadLocal<String>,String>>(){
            @Override
            protected Map<ThreadLocal<String>,String> initialValue() {
                return new HashMap<>();
            }
        };
        InheritableThreadLocal<Map<ThreadLocal<String>,String>> inheritableThreadLocal=new InheritableThreadLocal<Map<ThreadLocal<String>,String>>(){
            @Override
            protected Map<ThreadLocal<String>,String> initialValue() {
                return new HashMap<>();
            }
        };
        Map<ThreadLocal<String>,String> threadLocalStringMap=new HashMap<>();
        InheritableThreadLocal<String> str1=new InheritableThreadLocal<>();
        str1.set("str1");
        threadLocalStringMap.put(str1, "str1");
        ThreadLocal<String> str2=new ThreadLocal<String>(){
            @Override
            protected String initialValue() {
                return Thread.currentThread().getName();
            }
        };
        str2.set("str2");
        threadLocalStringMap.put(str2, "str2");

        threadLocal.set(threadLocalStringMap);
        inheritableThreadLocal.set(threadLocalStringMap);

        ExecutorService executorService= Executors.newFixedThreadPool(1);
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                System.out.println("threadLocal map size-->"+threadLocal.get().size());
                System.out.println("inheritableThreadLocal map size-->"+inheritableThreadLocal.get().size());

                ThreadLocal<String> test=new ThreadLocal<>();
                test.set("test");
                threadLocal.get().put(test, "test");
                System.out.println("threadLocal map put content-->"+threadLocal.get());

                for(Map.Entry<ThreadLocal<String>,String> entry :inheritableThreadLocal.get().entrySet()){
                    System.out.println(" sub thread inheritableThreadLocal map value-->"+entry.getKey().get()+",origal value-->"+entry.getValue());
                    String randomStr=RandomStringUtils.randomNumeric(6);
                    entry.getKey().set(randomStr);
                    entry.setValue(randomStr);
                    System.out.println("sub thread set new value-->"+randomStr);
                }
            }
        };

        System.out.println("================test one================");
        executorService.submit(runnable);

        executorService.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("================test one after================");

        System.out.println(threadLocal.get().equals(inheritableThreadLocal.get()));

        for(Map.Entry<ThreadLocal<String>,String> entry :inheritableThreadLocal.get().entrySet()){
            System.out.println("main thread  threadLocal value-->"+entry.getKey().get()+",origal value-->"+entry.getValue());
        }

        str1.set("new str");
        str2.set("new str2");

        System.out.println("================test two================");
        runnable=new Runnable() {
            @Override
            public void run() {
                System.out.println("threadLocal map size-->"+threadLocal.get().size()+",content-->"+threadLocal.get());
                System.out.println("inheritableThreadLocal map size-->"+inheritableThreadLocal.get().size());

                for(Map.Entry<ThreadLocal<String>,String> entry :inheritableThreadLocal.get().entrySet()){
                    System.out.println("sub thread  inheritableThreadLocal map value-->"+entry.getKey().get()+",origal value-->"+entry.getValue());
                    String randomStr=RandomStringUtils.randomNumeric(6);
                    entry.getKey().set(randomStr);
                    entry.setValue(randomStr);
                    System.out.println("sub thread set new value-->"+randomStr);
                }
            }
        };


        executorService.submit(runnable);

        executorService.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("================test two after================");


        System.out.println(threadLocal.get().equals(inheritableThreadLocal.get()));

        for(Map.Entry<ThreadLocal<String>,String> entry :inheritableThreadLocal.get().entrySet()){
            System.out.println("main thread  threadLocal value-->"+entry.getKey().get()+",origal value-->"+entry.getValue());
        }



    }

    @Test
    public void test2() throws Exception{
        TransmittableThreadLocal<String> str=new TransmittableThreadLocal<>();
        str.set("str");
        Runnable job= TtlRunnable.get(new Runnable() {
            @Override
            public void run() {
                System.out.println("str origal value-->"+str.get());
                str.set(RandomStringUtils.randomNumeric(6));
                System.out.println("str new value-->"+str.get());
            }
        });
        ExecutorService executorService=Executors.newFixedThreadPool(1);
        for(int i=0;i<3;i++){
            executorService.submit(job);
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

    }

    @Test
    public void test3() throws Exception{
        TransmittableThreadLocal<String> str=new TransmittableThreadLocal<>();
        str.set("str");
        Runnable job= new Runnable() {
            @Override
            public void run() {
                System.out.println("str origal value-->"+str.get());
                str.set(RandomStringUtils.randomNumeric(6));
                System.out.println("str new value-->"+str.get());
            }
        };
        ExecutorService executorService= TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(1));
        for(int i=0;i<3;i++){
            executorService.submit(job);
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

    }

    @Test
    public void test4() throws Exception{
        TransmittableThreadLocal<String> str=new TransmittableThreadLocal<>();
        str.set("str");
        Runnable job= new Runnable() {
            @Override
            public void run() {
                System.out.println("str origal value-->"+str.get());
                str.set(RandomStringUtils.randomNumeric(6));
                System.out.println("str new value-->"+str.get());
            }
        };
        ExecutorService executorService= Executors.newFixedThreadPool(1);
        for(int i=0;i<3;i++){
            executorService.submit(job);
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

    }


}
