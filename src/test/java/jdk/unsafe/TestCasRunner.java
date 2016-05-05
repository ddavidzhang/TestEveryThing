package jdk.unsafe;

/**
 * Created by junwei on 05/05/16.
 */

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestCasRunner {

    public static void main(String args[]) throws InterruptedException {

        int size = 10;

        CountDownLatch countDownLatch = new CountDownLatch(size);

        TomUnsafeRunner tomRunner = new TomUnsafeRunner(false, countDownLatch,
                "runner");

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 1; i <= size; i++) {
            executorService.execute(new Thread2RunUnsafe(countDownLatch,
                    tomRunner, i + "_号"));
        }
        countDownLatch.await();
        executorService.shutdown();

        // new Thread(volatileRunner).start();
    }

    static class Thread2RunUnsafe implements Runnable {
        private CountDownLatch countDownLatch;
        private TomUnsafeRunner tomRunner;
        private String name;

        public Thread2RunUnsafe(CountDownLatch countDownLatch,
                                TomUnsafeRunner tomRunner, String name) {
            super();
            this.countDownLatch = countDownLatch;
            this.tomRunner = tomRunner;
            this.name = name;
        }

        public void run() {
//            System.out.println(this.name + ":running...");
            this.tomRunner.doWork();
//            System.out.println(this.name + ":结束...");
            this.countDownLatch.countDown();

        }
    }

    static class TomUnsafeRunner {

        volatile boolean shutdownRequested = false;
        // boolean shutdownRequested = false;
        String name;
        int unsafe_var = 23;

        public TomUnsafeRunner(boolean shutdownRequested,
                               CountDownLatch countDownLatch, String name) {
            super();
            this.shutdownRequested = shutdownRequested;

            this.name = name;
        }

        public void shutdown() {
            this.shutdownRequested = true;
        }

        public void doWork() {


            ////////////////////一般写法///////////////
// if (unsafe_var == 23) {

// System.out.println(“我判断出来了，unsafe_var ==23，我设置为46..“);

// try {

// //模拟业务代码

// Thread.sleep(1000);

// } catch (InterruptedException e) {

// e.printStackTrace();

// }

// unsafe_var = 46;

// }
            /////////////////用JAVA CAS技术
            Unsafe unsafe = UnsafeSupport.getInstance();
            Class clazz = TomUnsafeRunner.class;
            Field[] fields = clazz.getDeclaredFields();
            System.out.println("fieldName:fieldOffset");
            // 获取属性偏移量，可以通过这个偏移量给属性设置
            // arg0, arg1, arg2, arg3 分别是目标对象实例，目标对象属性偏移量，当前预期值，要设的值
            // unsafe.compareAndSwapInt(arg0, arg1, arg2, arg3)
            // 偏移量编译后一般不会变的,intParam这个属性的偏移量
            // unsafe_var:8
            long intParamOffset = 0;
            for (Field f : fields) {
                System.out.println(f.getName() + ":"
                        + unsafe.objectFieldOffset(f));
                if (f.getName().equals("unsafe_var")) {
                    intParamOffset = unsafe.objectFieldOffset(f);
                }
            }

            if (unsafe.compareAndSwapInt(this, intParamOffset, 23, 46)) {
                try {
                    ////模拟业务代码
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("我判断出来了，unsafe_var == 23，我设置为46..");
            }
        }
    }

    static class UnsafeSupport {
        //private static Logger log = Logger.getLogger(UnsafeSupport.class);

        private static Unsafe unsafe;

        static {
            Field field;
            try {
                // 由反编译Unsafe类获得的信息
                field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                // 获取静态属性,Unsafe在启动JVM时随rt.jar装载
                unsafe = (Unsafe) field.get(null);
            } catch (Exception e) {
                //log.error("Get Unsafe instance occur error", e);
            }
        }

        /**
         * 获取{@link Unsafe }
         */
        public static Unsafe getInstance() {
            return unsafe;
        }
    }
}
