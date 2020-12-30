package server;

import config.Configuration;
import handler.RpcServerInitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/28 18:54
 */

public class RpcServer {

    //固定扫描的providerconfig文件路径，类路径下的/providerconfig目录
    private static final String PROPERTIES_PATH = RpcServer.class.getResource("/").getPath() + "/providerconfig";

    //服务端端口
    private int port;

    //上下文
    private Configuration configuration;



    public  RpcServer(int port,int serviceThreads){
        this.port = port;
        //创建一个固定业务线程池。
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                serviceThreads,serviceThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>()
        );
        configuration = new Configuration();
        configuration.setServiceExecutor(threadPoolExecutor);
    }

    public void start() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {

        //初始化Provider提供者
        initProvider();

        //指定两个线程
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(2);

        //默认使用cpu核心数*2的线程
        NioEventLoopGroup workGroup = new NioEventLoopGroup();


        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workGroup)
                //设置TCP等待队列大小
                .childOption(ChannelOption.SO_BACKLOG,128)
                .channel(NioServerSocketChannel.class)
                //设置初始化handler
                .childHandler(new RpcServerInitHandler(configuration));

        //绑定
        ChannelFuture future = serverBootstrap.bind(port).sync();
        System.out.println("服务器启动成功");
        future.channel().closeFuture().sync();
    }

    private void initProvider() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        ConcurrentHashMap<String,Object> providers = new ConcurrentHashMap<String, Object>();
        //用classpath:/providerconfig路径建立文件
        File folder = new File(PROPERTIES_PATH);
        if (!folder.exists()){
            //目录是否存在校验
            throw new FileNotFoundException("配置文件目录" + PROPERTIES_PATH + "不存在");
        }

        if (!folder.isDirectory()){
            //是否为目录校验
            throw new RuntimeException(PROPERTIES_PATH + "不是目录");
        }

        //获取该目录下所有文件
        File[] files = folder.listFiles();
        for (File file:files){
            //遍历
            if (file.isDirectory())
                //过滤掉目录，也就是说只会扫描该目录下的配置文件，不扫描子目录
                continue;

            //解析配置文件
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            String className = properties.getProperty("provider.class");
            String providerId = properties.getProperty("provider.id");

            if (StringUtils.isEmpty(className))
                throw new RuntimeException("provider.class不能为空,定位文件" + file.getAbsolutePath());

            //根据配置文件配置服务提供者的Class全类名加载Class对象
            Class<?> provider = Class.forName(className);

            //如果配置文件没有配置提供者的ID，就使用类名作为ID
            if (StringUtils.isEmpty(providerId)){
                providerId = provider.getSimpleName();
            }

            //判断服务提供者是否存在
            if (providers.contains(providerId)){
                throw new RuntimeException("服务提供者" + providerId + "已经存在，无法再次添加一样的");
            }

            //把服务提供者保存起来
            providers.put(providerId,provider.newInstance());

        }
        configuration.setProviders(providers);
    }


    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        RpcServer rpcServer = new RpcServer(5858,100);
        rpcServer.start();
    }
}
