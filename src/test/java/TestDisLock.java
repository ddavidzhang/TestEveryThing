import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.SingleServerConfig;
import org.redisson.core.RLock;

import java.util.concurrent.TimeUnit;

/**
 * Created by junwei on 29/03/16.
 */
public class TestDisLock {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("172.31.160.168:6379");
        RedissonClient redisson = Redisson.create(config);
        final RLock lock = redisson.getLock("anyLock");

// Lock time-to-live support
// releases lock automatically after 10 seconds
// if unlock method not invoked
//        lock.lock(10, TimeUnit.SECONDS);

// Wait for 100 seconds and automatically unlock it after 10 seconds
//        boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
//        lock.unlock();
        ThreadGroup threadGroup = new ThreadGroup("test lock");
        int threadNum=5;
        for (int i = 0; i < threadNum; i++) {
            Thread t = new Thread(threadGroup, new Runnable() {
                public void run() {
                    try {
                        lock.lock(10, TimeUnit.SECONDS);
                        System.out.println("thread " + Thread.currentThread().getName() + "get lock");
                    } catch (Exception e) {

                    }finally {
                        System.out.println("thread " + Thread.currentThread().getName() + "release lock");
                        lock.unlock();
                    }
                }
            });
            t.setName(""+i);
            t.start();
        }

        while (threadGroup.activeCount() > 0) {
            try {
                System.out.println("active count:"+threadGroup.activeCount());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        redisson.shutdown();
    }
}
