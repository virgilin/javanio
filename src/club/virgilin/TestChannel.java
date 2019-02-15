package club.virgilin;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * TestChannel
 *
 * @author virgilin
 * @date 2019/2/15
 *
 * 一、通道(Channel):用于源节点与目标节点的连接。在Java NIO中负责缓冲区中数据的传输。Channel本身不存储数据，因此需要配合缓冲区进行传输。
 *
 * 二、通道的主要实现类
 *   java.nio.channels.Channel 接口：
 *       |--FileChannel
 *       |--SocketChannel
 *       |--ServerSocketChannel
 *       |--DatagramChannel
 * 三、获取通道
 * 1.Java 针对支持通道的类提供了getChannel()方法
 *     本地 IO ：
 *     FileInputStream/FileOutputStream
 *     RandomAccessFile
 *
 *     网络 IO ：
 *     Socket
 *     ServerSocket
 *     DatagramSocket
 *
 * 2.在JDK 1.7 中的NIO.2 针对各个通道提供静态方法open()
 * 3.在JDK 1.7 中的NIO.2 的Files 工具类的 newByteChannel()
 *
 * 四、通道之间的数据传输
 * transferTo()
 * transferFrom()
 *
 * 五、分散（Scatter）与聚集（Gather）
 * 分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
 * 聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中
 *
 * 六、字符集：Charset
 * 编码：字符串 -> 字节数组
 * 解码：字节数组 -> 字符串
 */
public class TestChannel {

    /**
     * 字符集
     */
    @Test
    public void test6() throws CharacterCodingException {
        Charset cs1 = Charset.forName("GBK");
        CharsetEncoder ce = cs1.newEncoder();
        CharsetDecoder cd = cs1.newDecoder();
        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("你好，世界");
        cBuf.flip();
        /**
         * 编码
         */
        ByteBuffer bBuf = ce.encode(cBuf);
        for (int i = 0; i < bBuf.limit(); i++) {
            System.out.println(bBuf.get());
        }

        /**
         * 解码
         */
        bBuf.flip();
        CharBuffer cBuf2 = cd.decode(bBuf);
        System.out.println(cBuf2.toString());
    }

    @Test
    public void test5(){
        SortedMap<String, Charset> map = Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> entries = map.entrySet();
        for (Map.Entry<String, Charset> entry : entries) {
            System.out.println(entry.getKey()+"::::"+entry.getValue());
        }
    }

    /**
     * 4.分散于聚集
     */
    @Test
    public void test4() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("1.txt","rw");

        //1.获取通道
        FileChannel channel = raf.getChannel();

        //2.分配指定大小的缓冲区

        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        //3.分散读取
        ByteBuffer[] bufs = {buf1,buf2};
        channel.read(bufs);
        for (ByteBuffer byteBuffer : bufs) {
            byteBuffer.flip();
        }

        System.out.println(new String(bufs[0].array(),0,bufs[0].limit()));
        System.out.println("---------------------");
        System.out.println(new String(bufs[1].array(),0,bufs[1].limit()));

        //4.聚集写入
        RandomAccessFile randomAccessFile = new RandomAccessFile("2.txt","rw");
        FileChannel channel1 = randomAccessFile.getChannel();
        channel1.write(bufs);
    }

    /**
     * 3.通道之间的数据传输（直接缓冲区）
     * @throws IOException
     */
    @Test
    public void test3() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("4.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

//        inChannel.transferTo(0,inChannel.size(),outChannel);
        outChannel.transferFrom(inChannel,0,inChannel.size());
        inChannel.close();
        outChannel.close();
    }
    /**
     * 2.使用直接缓冲区完成文件的复制（内存映射文件）
     */

    @Test
    public void test2() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("3.jpg"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);
        //内存映射文件
        MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());
        //直接对缓冲区进行数据读写操作
        byte[] dst = new byte[inMappedBuf.limit()];
        inMappedBuf.get(dst);
        outMappedBuf.put(dst);
        inChannel.close();
        outChannel.close();
    }

    /**
     * 1.利用通道完成文件的复制(非直接缓冲区)
     */

    @Test
    public void test1(){

        FileInputStream fis = null;
        FileOutputStream fos = null;

        //①获取通道
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            fis = new FileInputStream("1.jpg");
            fos = new FileOutputStream("2.jpg");

            //①获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            //②分配指定大小的缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            while (inChannel.read(buf) != -1){
                buf.flip();//切换读取数据的模式
                outChannel.write(buf);
                buf.clear();//清空缓冲区
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inChannel != null){
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outChannel != null){
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
