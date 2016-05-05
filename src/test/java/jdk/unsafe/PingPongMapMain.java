package jdk.unsafe;

/**
 * Created by junwei on 05/05/16.
 */

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
/*
当你分别在两个程序，一个输入odd一个输入even，中运行时，可以看到两个进程都是通过持久化共享内存交换数据的。

在每个程序中，将相同的磁盘缓存映射到进程中。内存中实际上只有一份文件的副本存在。这意味着内存可以共享，前提是你使用线程安全的操作，比如volatile变量和CAS操作。（译注：CAS Compare and Swap 无锁算法）

在两个进程之间有83ns的往返时间。当考虑到System V IPC（进程间通信）大约需要2500ns，而且用IPC volatile替代persisted内存，算是相当快的了。

Unsafe适合在工作中使用吗？

个人不建议直接使用Unsafe。它远比原生的Java开发所需要的测试多。基于这个原因建议还是使用经过测试的库。如果你只是想自己用Unsafe，建议你最好在一个独立的类库中进行全面的测试。
这限制了Unsafe在你的应用程序中的使用方式，但会给你一个更安全的Unsafe。
 */
public class PingPongMapMain {
    public static void main(String... args) throws IOException {
        boolean odd;
        switch (args.length < 1 ? "usage" : args[0].toLowerCase()) {
            case "odd":
                odd = true;
                break;
            case "even":
                odd = false;
                break;
            default:
                System.err.println("Usage: java PingPongMain [odd|even]");
                return;
        }
        int runs = 10000000;
        long start = 0;
        File counters = new File(System.getProperty("java.io.tmpdir"), "counters.deleteme");
        counters.deleteOnExit();
        System.out.println("Waiting for the other odd/even,file:"+counters.getAbsolutePath());
        try (FileChannel fc = new RandomAccessFile(counters, "rw").getChannel()) {
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
            long address = ((DirectBuffer) mbb).address();
            for (int i = -1; i < runs; i++) {
                for (; ; ) {
                    long value = UNSAFE.getLongVolatile(null, address);
                    boolean isOdd = (value & 1) != 0;
                    if (isOdd != odd)
                    // wait for the other side.
                        continue;
                    // make the change atomic, just in case there is more than one odd/even process
                    if (UNSAFE.compareAndSwapLong(null, address, value, value + 1))
                        break;
                }
                if (i == 0) {
                    System.out.println("Started");
                    start = System.nanoTime();
                }
            }
        }
        System.out.printf("... Finished, average ping/pong took %,d ns%n",
                (System.nanoTime() - start) / runs);
    }

    static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
