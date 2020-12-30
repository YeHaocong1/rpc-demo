package handler;

import codc.MessageProtocolEncode;
import config.Configuration;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import stub.ClientStub;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 11:12
 */

public class RpcClientInitHandler extends ChannelInitializer<SocketChannel> {
    private Configuration configuration;

    private ClientStub clientStub;
    public RpcClientInitHandler(Configuration configuration, ClientStub clientStub){
        this.configuration = configuration;
        this.clientStub = clientStub;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //客户端的初始化handler
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
        //添加String；类型的编码解码器
        pipeline.addLast(new StringEncoder());

        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new MessageProtocolEncode());
        //添加一个自定义业务handler
        pipeline.addLast(new RpcClientServiceHandler(configuration,clientStub));
    }
}
