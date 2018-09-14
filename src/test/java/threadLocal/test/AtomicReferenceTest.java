package threadLocal.test;

import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {

    @Test
    public void test() throws Exception {
        Person people1 =new Person("Bom", 0);
        Person people2 =new Person("Tom",10);

        //先初始化一个值，如果不初始化则默认值为null
        AtomicReference<Person> reference = new AtomicReference<>(people1);
        Person people3 = reference.get();
        if (people3.equals(people1)) {
            System.out.println("people3:" + people3);
        } else {
            System.out.println("else:" + people3);
        }

        /**
         * 当前值：拿当前值和reference.get()获取到的值去比较，如果相等则true并更新值为期望值
         * 期望值：如果返回true则更新为期望值，如果返回false则不更新值
         */
        boolean b = reference.compareAndSet(null, people2);
        System.out.println("myClass.main-"+b+"--"+reference.get());

        boolean b1 = reference.compareAndSet(people1, people2);
        System.out.println("myClass.main-"+b1+"--"+reference.get());


        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread1-----------");

                Person people = reference.get();
                people.setName("Tom1");
                people.setAge(people.getAge()+1);
                reference.getAndSet(people);
                System.out.println("Thread1:"+reference.get().toString());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread2-----------");

                Person people = reference.get();
                people.setName("Tom2");
                people.setAge(people.getAge()+1);
                reference.getAndSet(people);
                System.out.println("Thread2:"+reference.get().toString());
            }
        }).start();

    }

    @Test
    public void test2() throws Exception {
        final AtomicReference<Person> personAtomicReference=new AtomicReference<>(new Person("shl",12));
        Runnable job1=new Runnable() {
            @Override
            public void run() {
                Person person=personAtomicReference.get();
                person.setAge(person.getAge()+1);
            }
        };
        ExecutorService executor= Executors.newFixedThreadPool(20);
        for(int i=0;i<1000;i++){
            executor.submit(job1);
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println(personAtomicReference.get());

    }
}

class Person {
    private String name;
    private int age;

     Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String toString() {
        return "[name: " + this.name + ", age: " + this.age + "]";
    }
}

