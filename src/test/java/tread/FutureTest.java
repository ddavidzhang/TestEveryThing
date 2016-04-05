package tread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by junwei on 01/04/16.
 */
public class FutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Task> taskFutureTask = new FutureTask<Task>(new Task());
        Thread thread = new Thread(taskFutureTask);
        thread.start();
        Object result = taskFutureTask.get();
        System.out.println(result);
    }

    static class Task implements Callable {
        public Object call() throws Exception {
            return "good";
        }
    }
}
