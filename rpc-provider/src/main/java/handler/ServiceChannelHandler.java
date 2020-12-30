package handler;

import config.Configuration;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import runable.ServiceRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/28 19:05
 */
//具体自定义handler
public class ServiceChannelHandler extends SimpleChannelInboundHandler<String> {

    private Configuration configuration;

    public ServiceChannelHandler(Configuration configuration){
        this.configuration = configuration;
    }

    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        //从业务线程池中取出一个线程用于执行业务逻辑代码，使得Netty的线程只需负责Io和转发
        configuration.getServiceExecutor().execute(new ServiceRunnable(msg, configuration,ctx));
    }
}
