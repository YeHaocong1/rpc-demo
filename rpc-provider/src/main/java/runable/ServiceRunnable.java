package runable;

import config.Configuration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/28 19:45
 */

public class ServiceRunnable implements Runnable{

    //    myRpc://HelloServiceImpl@@##sayHallo@@##java.lang.String@@##Yehaocong
    //URL解析：
    // myRpc://  固定开头
    //HelloServiceImpl 服务提供者名称
    //sayHallo  要调用的方法
    //java.lang.String 方法的参数类型
    //Yehaocong  方法的值
    //上面各个之间用@@##分隔
    //然后方法参数类型和方法参数的值会存在多个 ，每个用##@@分隔



    private String msg;

    private  static String protocolString = "myRPC://";

    private Configuration configuration;

    private ChannelHandlerContext ctx;

    private ConcurrentHashMap<String,Object> providers;

    public ServiceRunnable(String msg,Configuration configuration,ChannelHandlerContext ctx){
        this.configuration = configuration;
        this.msg = msg;
        this.providers = configuration.getProviders();
        this.ctx = ctx;
    }




    @Override
    public void run() {
        Channel channel = ctx.channel();

        if (StringUtils.isEmpty(msg)){
            channel.writeAndFlush("url不能为空");
            return;
        }

        //如果url不是以myRpc://开头，返回给服务消费方错误信息。
        if (!msg.startsWith("myRpc://")){
            channel.writeAndFlush("url必须以myRPC://开头");
            return;
        }

        String msgWithProtocolString = msg.substring(protocolString.length());
        String[] elements = msgWithProtocolString.split("@@##");
        String providerId = elements[0];
        Object provider = providers.get(providerId);
        //如果服务消费方传过来的服务providerId在服务端不存在的话，就返回错误信息。
        if (provider == null){
            channel.writeAndFlush("providerId为" + providerId + "的服务提供者不存在");
            return;
        }

        Class providerClass = provider.getClass();




        String methodName = elements[1];
        String[] paramTypeNames = elements[2].split("##@@");
        Class[] paramTypes = new Class[paramTypeNames.length];
        for (int i = 0;i<paramTypeNames.length;i++){
            String paramTypeName = paramTypeNames[i];
            Class<?> aClass = null;
            try {
                aClass = Class.forName(paramTypeName);
            } catch (ClassNotFoundException e) {
                //参数类型的类不存在，就返回错误信息
                channel.writeAndFlush("参数类型"+paramTypeName+"不存在");
                return;
            }
            paramTypes[i] = aClass;
        }

        Method method = null;
        try {
            method = providerClass.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            //方法不存在返回错误信息
            channel.writeAndFlush("方法"+methodName+"不存在");
            return;
        }

        Object[] objectParams = elements[3].split("##@@");
        Object obj = null;
        try {
            //执行方法
            obj = method.invoke(provider, objectParams);
        } catch (Exception e) {
            channel.writeAndFlush("方法"+methodName+"执行出错");
            return;
        }

        //将结果回写客户端
        channel.writeAndFlush(obj);
    }
}
