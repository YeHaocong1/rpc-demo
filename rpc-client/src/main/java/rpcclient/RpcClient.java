package rpcclient;

import config.Configuration;
import handler.RpcClientInitHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import stub.ClientStub;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 10:56
 */

public class RpcClient {

    private  int port;

    private Configuration configuration;

    private String host;

    public RpcClient(String host,int port,Configuration configuration){
        this.host= host;
        this.port = port;
        this.configuration = configuration;
    }


    public void start(ClientStub clientStub) throws InterruptedException {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitHandler(configuration,clientStub));

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            System.out.println("客户端连接服务端:" + host + ":" + port + "成功");
            channelFuture.channel().closeFuture().sync();
        }finally {
            worker.shutdownGracefully();
        }
    }


}
