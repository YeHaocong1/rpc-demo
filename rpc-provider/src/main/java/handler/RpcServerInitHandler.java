package handler;

import codc.MessageProtocolEncode;
import config.Configuration;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author YeHaocong
 * @decription 初始化handler
 * @Date 2020/12/28 19:02
 */


public class RpcServerInitHandler extends ChannelInitializer<SocketChannel> {

    private Configuration configuration;
    public RpcServerInitHandler(Configuration configuration){
        this.configuration = configuration;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //添加一个String类型的编解码器和一个业务处理类。
        //也就是说该rpc框架目前只允许String类型的数据传输
        //添加一个解码器来解决粘包拆包问题
        //参数1为 包的最大长度，超过的部分会被丢掉，第二个参数表示表示长度的字节在数据包的位置，这里就0，在包头。
        //第三个参数表示表示数据长度的字节的大小，这里在包头用int型表示数据包的长度，所以是4.
        //最后一个参数表示要跳过的字节数，因为实际数据不包含表示数据长度的字节，所以要过滤4个字节
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new ServiceChannelHandler(configuration));
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new MessageProtocolEncode());
    }
}
