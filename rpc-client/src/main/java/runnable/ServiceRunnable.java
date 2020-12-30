package runnable;

import config.Configuration;
import config.ConsumerConfigEntity;
import handler.RpcClientServiceHandler;
import stub.ClientStub;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/30 11:44
 */

public class ServiceRunnable implements Callable<String> {

    private final Method method;
    private final ConsumerConfigEntity consumerConfigEntity;
    private final Object[] args;
    private final Configuration configuration;
    private final ClientStub clientStub;

    public ServiceRunnable(Method method,ConsumerConfigEntity consumerConfigEntity,Object[] args,
                           Configuration configuration,ClientStub clientStub){
        this.method = method;
        this.consumerConfigEntity = consumerConfigEntity;
        this.args = args;
        this.configuration = configuration;
        this.clientStub = clientStub;
    }

    @Override
    public String call() throws Exception {
        //获取方法名
        String methodName = method.getName();
        //获取参数类型列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        StringBuilder url = new StringBuilder();
        //拼接URL
        //     协议头     提供者id           方法       方法参数类型列表       参数值列表
        //最终myRpc://HelloServiceImpl@@##sayHallo@@##java.lang.String@@##Yehaocong
        url.append("myRpc://").append(consumerConfigEntity.getProviderId()).append("@@##").append(methodName);
        if (parameterTypes.length > 0) {
            url.append("@@##").append(parameterTypes[0].getName());
        }
        for (int i = 1; i < parameterTypes.length; i++) {
            url.append("##@@").append(parameterTypes[i].getName());
        }

        if (args != null && args.length > 0) {
            url.append("@@##").append(args[0]);
        }
        if (args != null) {
            for (int i = 1; i < args.length; i++) {
                url.append("##@@").append(args[i]);
            }
        }

        String host = consumerConfigEntity.getProviderHost();
        int port = consumerConfigEntity.getProviderPort();
        String key = host + ":" + port;
        //这个以 host:port为key   value为channelhandlercontext数组的 map
        Map<String, RpcClientServiceHandler> rpcClientServiceHandlersMap = configuration.getRpcClientServiceHandlerMap();
        RpcClientServiceHandler rpcClientServiceHandler = rpcClientServiceHandlersMap.get(key);
        String result = null;
        synchronized (rpcClientServiceHandlersMap){
        if (rpcClientServiceHandler == null) {
            //如果这个为空或者大小为0，证明  host:port这个服务端的连接还没建立，然后在这里建立，属于懒加载的一种手段。
            //创建客户端
            //双重检查避免锁避免重复创建连接到同一服务端的客户端
            synchronized (configuration) {
                rpcClientServiceHandler = rpcClientServiceHandlersMap.get(key);
                if (rpcClientServiceHandler == null) {
                    //新建一个线程来创建netty客户端并连接到服务端
                    new Thread(new CreateClientRunnable(clientStub, host, port, configuration)).start();
                    //当前线程等待，会在client启动完成时才唤醒，确保client建立完成才继续下面的代码
                    configuration.wait();
                }

            }
//

        }
    }
        //重新获取，因为此时客户端已经建立完成并连接到服务端，所以不会为空
        rpcClientServiceHandler = configuration.getRpcClientServiceHandlerMap().get(key);

        result = rpcClientServiceHandler.sendMsg(url.toString());
        return result;
    }
}
