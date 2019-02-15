package club.virgilin;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

/**
 * TestNonBlockingNIO
 *
 * @author virgilin
 * @date 2019/2/15
 *
 * 一、使用NIO完成网络通信的三个核心：
 * 1.通道（Channel）：负责连接
 *
 *     java.nio.channels.Channel 接口：
 *         |--SelectableChannel
 *             |--SocketChannel
 *             |--ServerSocketChannel
 *             |--DatagramChannel
 *
 *             |--Pipe.SinkChannel
 *             |--Pipe.SourceChannel
 * 2.缓冲区（Buffer）：负责数据的存取
 *
 * 3.选择器（Selector）：是SelectableChannel 的多路复用器，用于监听SelectableChannel的IO状况
 *
 */
public class TestNonBlockingNIO {

    public static void main(String[] args) throws IOException {
        //1.获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

        //2.切换非阻塞模式
        socketChannel.configureBlocking(false);

        //3.分配指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //4.发送数据给服务端
        System.out.println("请输入：");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String str = scanner.nextLine();
            buffer.put((new Date().toString() + "\n" + str).getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }


        //5.关闭通道
        socketChannel.close();
    }
    @Test
    public void scanner(){
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入你想说的：");;
        String str = null;
        str = sc.next();

        System.out.println(str);

    }

    /**
     * 客户端
     */
    @Test
    public void client() throws IOException {
        //1.获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

        //2.切换非阻塞模式
        socketChannel.configureBlocking(false);

        //3.分配指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //4.发送数据给服务端
//        System.out.println("请输入：");
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNext()){
//            String str = scanner.next();
//            buffer.put((new Date().toString() + "\n" + str).getBytes());
//            buffer.flip();
//            socketChannel.write(buffer);
//            buffer.clear();
//        }
        System.out.println("请输入数据：");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            System.out.println(scanner.next());
        }
        System.out.println("发送完成");
        buffer.put((new Date().toString()).getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();


        //5.关闭通道
        socketChannel.close();
    }

    /**
     * 服务端
     */

    @Test
    public void server() throws IOException {

        //1.获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //2.切换非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //3.绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(9898));

        //4.获取选择器
        Selector selector = Selector.open();

        //5.将通道注册到选择器上，并指定“监听接收时间”
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);


        //6.轮询式的获取选择器上已经“准备就绪”的事件
        while (selector.select()>0){
            //7.获取当前选择器中所有注册的“选择键（已就绪的监听事件）”
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                //8.获取准备“就绪”的事件
                SelectionKey selectionKey = iterator.next();
                //9.判断具体是什么事件准备就绪
                if (selectionKey.isAcceptable()){
                    //10.若“接收就绪”，获取客户端连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //11.切换非阻塞模式
                    socketChannel.configureBlocking(false);
                    //12.将该通道注册到选择器上
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if (selectionKey.isReadable()){
                    //13.获取当前选择器上“读就绪”状态的通道
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    //14.读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int len = 0;
                    while ((len = channel.read(buffer)) > 0){
                        buffer.flip();
                        System.out.println(new String(buffer.array(),0,len));
                        buffer.clear();
                    }
                }
                //15.取消选择键SelectionKey
                iterator.remove();
            }
        }

    }
}
