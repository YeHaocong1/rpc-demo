package handler;

import config.Configuration;
import io.netty.channel.*;
import stub.ClientStub;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 11:28
 */

public class RpcClientServiceHandler extends ChannelInboundHandlerAdapter {

    private String result;

    private Configuration configuration;

    private ChannelHandlerContext context;

    private ClientStub clientStub;

    public RpcClientServiceHandler(Configuration configuration,ClientStub clientStub){
        this.configuration = configuration;
        this.clientStub  =clientStub;
    }


    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //服务端返回结果会进入这里
        if (msg == null)
            result = null;
        else {
            result = msg.toString();
        }
        //唤醒sendMsg的线程继续执行，返回结果给消费者
        notify();
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        synchronized (configuration){
        //到这里证明连接建立成功
        this.context = ctx;
        //添加当前对象到上下文中
        Map<String, RpcClientServiceHandler> rpcClientServiceHandlers = configuration.getRpcClientServiceHandlerMap();
        Channel channel = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String host = socketAddress.getHostString();
        int port = socketAddress.getPort();
        String key = host + ":" + port;
        RpcClientServiceHandler rpcClientServiceHandler = rpcClientServiceHandlers.get(key);
        if (rpcClientServiceHandler == null){
            rpcClientServiceHandler = this;
            rpcClientServiceHandlers.put(key,rpcClientServiceHandler);
        }

            //建立完成，把当前RpcClientServiceHandler对象添加到上下文中后，唤醒clientStub创建该连接的线程。
        configuration.notifyAll();
        }
        super.channelActive(ctx);
    }


    //发送消息
    public synchronized String sendMsg(String url){
        //发送消息
        context.channel().writeAndFlush(url);
        try {
            //阻塞当前线程，知道有结果返回。
            wait();
            return result;
        } catch (InterruptedException e) {
            return null;
        }
    }
}
