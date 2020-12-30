package stub;

import config.Configuration;
import config.ConsumerConfigEntity;
import handler.RpcClientServiceHandler;
import rpcclient.RpcClient;
import runnable.CreateClientRunnable;
import runnable.ServiceRunnable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author YeHaocong
 * @decription 代理类
 * @Date 2020/12/29 10:26
 */

//动态代理ClientStub的InvocationHandler
public class ClientStub implements InvocationHandler {
    private Configuration configuration;

    private ConsumerConfigEntity consumerConfigEntity;

    //要被代理的接口的Class对象，因为不需要实际执行方法，所以无需实现类，只需一个接口就行。
    private Class targetClazz;

    public ClientStub(Class targetClazz,Configuration configuration,ConsumerConfigEntity consumerConfigEntity){
        this.targetClazz = targetClazz;
        this.configuration = configuration;
        this.consumerConfigEntity = consumerConfigEntity;
    }

//    public Object getStub(){
//        Object o = Proxy.newProxyInstance(ClientStub.class.getClassLoader(), new Class[]{targetClazz}, this);
//        System.out.println(o);
//        return o;
//    }

    public  Object getStub(){
        return Proxy.newProxyInstance(targetClazz.getClassLoader(), new Class[]{targetClazz},this);
    }

    //调用消费者方法时，实际上就是调用这个方法。实际就是拼接url发送到服务端
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Future<String> future = configuration.getServiceThreadPool().submit(new ServiceRunnable(method, consumerConfigEntity, args, configuration, this));
        return future.get();

    }
}
