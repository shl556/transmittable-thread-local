package threadLocal.test;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
        test.set();
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

    @Test
    public void test5() throws Exception{
        ExecutorService executors= Executors.newFixedThreadPool(1);
        final StudentTest3 test=new StudentTest3();
        System.out.println("============init============");
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.getInheritable());
                test.set();
                System.out.println("============thread after set============");
                System.out.println(test.getInheritable());
            }
        };
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.getInheritable());
        //父线程改变了InheritableThreadLocal变量对应的对象，子线程获取的还是原来的对象
        test.set();
        System.out.println("============init two============");
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.getInheritable());
    }

    @Test
    public void test6() throws Exception{
        ExecutorService executors= Executors.newFixedThreadPool(1);
        final StudentTest4 test=new StudentTest4();
        System.out.println("============init============");
        System.out.println(test.getInheritable());
        Runnable job=new Runnable() {
            @Override
            public void run() {
                System.out.println("============thread before set============");
                System.out.println(test.getInheritable());
                test.set();
                System.out.println("============thread after set============");
                System.out.println(test.getInheritable());
            }
        };
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test one============");
        System.out.println(test.getInheritable());
        //父线程改变了InheritableThreadLocal变量对应的对象，子线程获取的还是原来的对象
        test.set();
        System.out.println("============init two============");
        System.out.println(test.getInheritable());
        executors.submit(job);
        executors.awaitTermination(1000, TimeUnit.MICROSECONDS);
        System.out.println("============test two============");
        System.out.println(test.getInheritable());
    }

    @Test
    public void test7() {
        Map<String,String> test=new HashMap<>();
        test.put("1", "a");
        test.put("2", "a");
        test.put("3", "a");
        for(Iterator<Map.Entry<String,String>> iterator=test.entrySet().iterator();iterator.hasNext();){
            Map.Entry<String,String> entry=iterator.next();
            System.out.println(entry.getKey()+":"+entry.getValue());
            if(entry.getKey().equals("3")){
                iterator.remove();
            }
        }
        System.out.println(test.toString());
    }

    @Test
    public void test8() throws Exception {
        Logger logger= LoggerFactory.getLogger(getClass());
        ExecutorService executors= Executors.newFixedThreadPool(1);
        TransmittableThreadLocal<String> str=new TransmittableThreadLocal<>();
        TransmittableThreadLocal<String> str2=new TransmittableThreadLocal<>();
        str.set("str1");
        str2.set("str2");

        System.out.println("==================init===============");
        System.out.println("str-->"+str.get());
        System.out.println("str2-->"+str2.get());

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                System.out.println("==============before set==============");
                System.out.println("str-->"+str.get());
                System.out.println("str2-->"+str2.get());

                str.set(RandomStringUtils.randomNumeric(5));
                str2.set(RandomStringUtils.randomNumeric(5));

                System.out.println("==============after set==============");
                System.out.println("str-->"+str.get());
                System.out.println("str2-->"+str2.get());


                TransmittableThreadLocal<String> test=new TransmittableThreadLocal<>();
                test.set(RandomStringUtils.randomNumeric(5));
                System.out.println("add new-->"+test.get());
            }
        };

        Runnable job=TtlRunnable.get(runnable);

        TransmittableThreadLocal<String> str3=new TransmittableThreadLocal<>();
        TransmittableThreadLocal<String> str4=new TransmittableThreadLocal<>();
        str3.set("str3");
        str4.set("str4");

        System.out.println("===============test one===============");
        executors.submit(job);
        executors.awaitTermination(5,TimeUnit.SECONDS);


        TransmittableThreadLocal<String> str5=new TransmittableThreadLocal<String>(){
            @Override
            protected String initialValue() {
                return "str5";
            }
        };
        TransmittableThreadLocal<String> str6=new TransmittableThreadLocal<String>(){
            @Override
            protected String initialValue() {
                return "str6";
            }
        };
        str5.get();
        str6.get();

        str.set("new str");
        str2.set("new str2");


        System.out.println("===============test two===============");
        executors.submit(job);
        executors.awaitTermination(5,TimeUnit.SECONDS);


        System.out.println("===============test three===============");
        job=TtlRunnable.get(runnable);
        executors.submit(job);
        executors.awaitTermination(5,TimeUnit.SECONDS);


    }
}

class StudentTest4{

    private Random random=new Random();

    private static InheritableThreadLocal<Map<TransmittableThreadLocal<Student>,String>> inheritableThreadLocalMap=new InheritableThreadLocal<Map<TransmittableThreadLocal<Student>,String>>(){
        @Override
        protected Map<TransmittableThreadLocal<Student>, String> initialValue() {
            return new HashMap<>();
        }
    };


    public StudentTest4() {
        TransmittableThreadLocal<Student> studentTransmittableThreadLocal=new TransmittableThreadLocal<Student>(){
            @Override
            protected Student initialValue() {
                Student student=new Student();
                student.set("shl");
                return student;
            }
        };
        inheritableThreadLocalMap.get().put(studentTransmittableThreadLocal,"shl");
        TransmittableThreadLocal<Student> studentTransmittableThreadLocal2=new TransmittableThreadLocal<Student>(){
            @Override
            protected Student initialValue() {
                Student student=new Student();
                student.set("shl2");
                return student;
            }
        };
        inheritableThreadLocalMap.get().put(studentTransmittableThreadLocal2,"shl2");
    }

    public String getInheritable(){
        StringBuilder stringBuilder=new StringBuilder();
        for(TransmittableThreadLocal<Student> key:inheritableThreadLocalMap.get().keySet()){
            stringBuilder.append(key.get().des()+"------"+inheritableThreadLocalMap.get().get(key)+System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    public void set(){
        for(TransmittableThreadLocal<Student> key:inheritableThreadLocalMap.get().keySet()){
            key.get().set(""+random());
        }
    }

    public void setNew(){
//        Student student=new Student();
//        student.set("inheritableThreadLocal:"+random());
//        inheritableThreadLocal.set(student);
    }


    private int random(){
        return random.nextInt();
    }


}


class StudentTest3{

    private Random random=new Random();

    private Map<TransmittableThreadLocal<Student>,String> inheritableThreadLocalMap=new HashMap<>();


    public StudentTest3() {
        TransmittableThreadLocal<Student> studentTransmittableThreadLocal=new TransmittableThreadLocal<Student>(){
            @Override
            protected Student initialValue() {
                Student student=new Student();
                student.set("shl");
                return student;
            }
        };
        inheritableThreadLocalMap.put(studentTransmittableThreadLocal,"shl");
        TransmittableThreadLocal<Student> studentTransmittableThreadLocal2=new TransmittableThreadLocal<Student>(){
            @Override
            protected Student initialValue() {
                Student student=new Student();
                student.set("shl2");
                return student;
            }
        };
        inheritableThreadLocalMap.put(studentTransmittableThreadLocal2,"shl2");
    }

    public String getInheritable(){
        StringBuilder stringBuilder=new StringBuilder();
        for(TransmittableThreadLocal<Student> key:inheritableThreadLocalMap.keySet()){
            stringBuilder.append(key.get().des()+"------"+inheritableThreadLocalMap.get(key)+System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    public void set(){
        for(TransmittableThreadLocal<Student> key:inheritableThreadLocalMap.keySet()){
            key.get().set(""+random());
        }
    }

    public void setNew(){
//        Student student=new Student();
//        student.set("inheritableThreadLocal:"+random());
//        inheritableThreadLocal.set(student);
    }


    private int random(){
        return random.nextInt();
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
