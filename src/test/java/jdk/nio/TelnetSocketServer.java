package jdk.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangyi on 16/3/15.
 */
public class TelnetSocketServer {

    public static void main(String[] args) throws Throwable{


        Selector selector = Selector.open();
        InetSocketAddress address = new InetSocketAddress(8080);

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(address);
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        AtomicInteger counter = new AtomicInteger(0);

        while(Thread.currentThread().isInterrupted() == false){
            try{

                long startTime = System.currentTimeMillis();
                int k = selector.select(5000);

                System.out.println("selector wakeup with return="+k+" for select time["+(System.currentTimeMillis() - startTime)+"]");

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while(it.hasNext()){
                    SelectionKey sk = it.next();
                    it.remove();
                    if(sk.isValid() == false) continue;
                    try{
                        if(sk.isAcceptable()){

                            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

                            SocketChannel sc = ssc.accept();
                            sc.socket().setSendBufferSize(64000);
                            sc.configureBlocking(false);
                            sc.register(selector, SelectionKey.OP_READ, buffer);

                            System.out.println("[new connection create] [client: "+sc.getRemoteAddress()+"]");
                        }
                        else if(sk.isReadable()){
                            SocketChannel sc = (SocketChannel)sk.channel();
                            System.out.println("[selectionKey readable] [client: " + sc.getRemoteAddress() + "]");

                            ByteBuffer buffer = (ByteBuffer) sk.attachment();
                            buffer.clear();

                            sc.read(buffer);
                            buffer.flip();
                            byte[] bytes = new byte[buffer.limit()];
                            buffer.get(bytes);
                            String read = new String(bytes);
                            System.out.println("[read] [client: " + sc.getRemoteAddress() + "] [in: "+read.trim()+"] [out: hello]");

                            buffer.clear();
                            buffer.put(("==> echo from server : hello " + counter.incrementAndGet() + "\r\n").getBytes());
                            buffer.flip();
                            sc.write(buffer);
                        }
                    } catch(Throwable e){
                        System.err.println("unkown error while handle selected key in Thread[" + Thread.currentThread().getName() + "]");
                        e.printStackTrace();

                        sk.channel().close();
                    }
                }

            }catch(Throwable e){
                System.err.println("unkown error in Thread[" + Thread.currentThread().getName() + "]");
                e.printStackTrace();
            }
        }

        System.out.println("Thread[" + Thread.currentThread().getName() + "] will be stopped");
    }

}
