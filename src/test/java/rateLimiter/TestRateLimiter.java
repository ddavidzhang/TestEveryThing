package rateLimiter;

import com.google.common.util.concurrent.RateLimiter;

/**
 * Created by zhangjw on 12/28/17.
 */
public class TestRateLimiter {
    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = RateLimiter.create(2);
        while (true) {
            //System.out.println(limiter.tryAcquire(5));
            Thread.sleep(200);
            //System.out.println(limiter.tryAcquire(4));
            System.out.println(limiter.tryAcquire(1));
            System.out.println(limiter.tryAcquire(1));
            System.out.println(limiter.tryAcquire(1));
            System.out.println(limiter.tryAcquire(1));
            System.out.println(limiter.tryAcquire(1));
            Thread.sleep(800);
            System.out.println(limiter.getRate());
        }
    }
}
