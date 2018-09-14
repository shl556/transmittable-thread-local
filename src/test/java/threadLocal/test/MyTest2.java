package threadLocal.test;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyTest2 {

    @Test
    public void test() throws Exception {
        final StudentTest test=new StudentTest();
        System.out.println("============init============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.get());
                System.out.println(test.getThreadLocal());
                System.out.println(test.getInheritable());
                test.set();
                System.out.println("============thread after set============");
                System.out.println(test.get());
                System.out.println(test.getThreadLocal());
                System.out.println(test.getInheritable());
            }
        });
        thread.start();
        thread.join();
        System.out.println("============test============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());

    }

    @Test
    public void test2() throws Exception {
        ExecutorService executors=Executors.newFixedThreadPool(1);
        final StudentTest test=new StudentTest();
        System.out.println("============init============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.get());
                //threadLocal变量在子线程中重新初始化,且初始化在线程创建的时候完成
                System.out.println(test.getThreadLocal());
                //InheritableThreadLocal变量是继承父线程的变量，即线程创建的时候会复制一份父线程中相同变量的引用，
                // 最终指向同一个实例，行为基本等价于普通的全局变量
                //因为复制引用行为是在线程创建时执行的，所以使用线程池的情形无法保证父子线程中的InheritableThreadLocal变量指向同一个实例
                System.out.println(test.getInheritable());
                test.set();
                System.out.println("============thread after set============");
                System.out.println(test.get());
                System.out.println(test.getThreadLocal());
                System.out.println(test.getInheritable());
            }
        };
        executors.submit(job);
        executors.awaitTermination(123, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
        test.set();
        System.out.println("============init two============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(123, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
    }

    @Test
    public void test3() throws Exception {
        ExecutorService executors=Executors.newFixedThreadPool(1);
        final StudentTest test=new StudentTest();
        System.out.println("============init============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.get());
                System.out.println(test.getThreadLocal());
                System.out.println(test.getInheritable());
                //如果子线程中改变了InheritableThreadLocal变量引用的对象，子线程复用时父子线程的InheritableThreadLocal变量变成了两个相互独立的变量
                test.setNew();
                System.out.println("============thread after set============");
                System.out.println(test.get());
                System.out.println(test.getThreadLocal());
                System.out.println(test.getInheritable());
            }
        };
        executors.submit(job);
        executors.awaitTermination(123, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
        test.set();
        System.out.println("============init two============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(123, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.get());
        System.out.println(test.getThreadLocal());
        System.out.println(test.getInheritable());
    }
}

class StudentTest{
    private Student student=new Student();

    private Random random=new Random();

    private ThreadLocal<Student> threadLocal=new ThreadLocal<Student>(){
        @Override
        protected Student initialValue() {
            return new Student();
        }
    };

    private InheritableThreadLocal<Student> inheritableThreadLocal=new InheritableThreadLocal<Student>(){
        @Override
        protected Student initialValue() {
            return new Student();
        }
    };

    public String get(){
        return student.des();
    }

    public String getThreadLocal(){
        return threadLocal.get().des();
    }

    public String getInheritable(){
        return inheritableThreadLocal.get().des();
    }

    public void set(){
        student.set("shl:"+random());
        Student student2=threadLocal.get();
        student2.set("threadLocal:"+random());
        Student student3=inheritableThreadLocal.get();
        student3.set("inheritableThreadLocal:"+random());
    }

    public void setNew(){
        student=new Student();
        Student student2=new Student();
        student2.set("threadLocal:"+random());
        threadLocal.set(student2);
        Student student3=new Student();
        student3.set("inheritableThreadLocal:"+random());
        inheritableThreadLocal.set(student3);
    }


    private int random(){
        return random.nextInt();
    }


}

class Student{
    private String userName;

    public Student(){
        this.userName=Thread.currentThread().getName();
    }


    public void set(String userName){
        this.userName=userName;
    }

    public String des(){
        return "userName:"+userName;
    }

}
