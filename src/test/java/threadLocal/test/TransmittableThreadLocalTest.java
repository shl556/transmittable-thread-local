package threadLocal.test;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransmittableThreadLocalTest {

    @Test
    public void test() throws Exception {
        //跟InheritableThreadLocal一样
        ExecutorService executors= Executors.newFixedThreadPool(1);
        final StudentTest2 test=new StudentTest2();
        System.out.println("============init============");
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.getInheritable());
                test.setNew();
                System.out.println("============thread after set============");
                System.out.println(test.getInheritable());
            }
        };
        executors.submit(job);
        executors.awaitTermination(500, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.getInheritable());
        test.set();
        System.out.println("============init two============");
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(500, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.getInheritable());
    }

    @Test
    public void test2() throws Exception {
        //子线程复用时获取的InheritableThreadLocal始终父线程一致
        ExecutorService executors= Executors.newFixedThreadPool(1);
        final StudentTest2 test=new StudentTest2();
        System.out.println("============init============");
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.getInheritable());
                test.setNew();
                System.out.println("============thread after set============");
                System.out.println(test.getInheritable());
            }
        };
        job= TtlRunnable.get(job);
        executors.submit(job);
        executors.awaitTermination(500, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.getInheritable());
        //修改父线程中的InheritableThreadLocal变量，子线程获取的是父线程修改后的值
        test.set();
        System.out.println("============init two============");
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(500, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.getInheritable());
    }

    @Test
    public void test3() throws Exception {
        ExecutorService executors= Executors.newFixedThreadPool(1);
        final StudentTest2 test=new StudentTest2();
        System.out.println("============init============");
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.getInheritable());
                test.setNew();
                System.out.println("============thread after set============");
                System.out.println(test.getInheritable());
            }
        };
        job= TtlRunnable.get(job);
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.getInheritable());
        //父线程改变了InheritableThreadLocal变量对应的对象，子线程获取的还是原来的对象
        test.setNew();
        System.out.println("============init two============");
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.getInheritable());
    }

    @Test
    public void test4() throws Exception {
        ExecutorService executors= Executors.newFixedThreadPool(1);
        executors = TtlExecutors.getTtlExecutorService(executors);
        final StudentTest2 test=new StudentTest2();
        System.out.println("============init============");
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.getInheritable());
                test.setNew();
                System.out.println("============thread after set============");
                System.out.println(test.getInheritable());
            }
        };
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.getInheritable());
        test.set();
        System.out.println("============init two============");
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.getInheritable());
    }


}

class StudentTest2{

    private Random random=new Random();

    private TransmittableThreadLocal<Student> inheritableThreadLocal=new TransmittableThreadLocal<Student>(){
        @Override
        protected Student initialValue() {
            return new Student();
        }
    };


    public String getInheritable(){
        return inheritableThreadLocal.get().des();
    }

    public void set(){
        Student student3=inheritableThreadLocal.get();
        student3.set("inheritableThreadLocal:"+random());
    }

    public void setNew(){
        Student student=new Student();
        student.set("inheritableThreadLocal:"+random());
        inheritableThreadLocal.set(student);
    }


    private int random(){
        return random.nextInt();
    }


}
