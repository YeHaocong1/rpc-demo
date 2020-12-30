package bootstrap;

import config.Configuration;
import config.ConsumerConfigEntity;
import org.apache.commons.lang3.StringUtils;
import stub.ClientStub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 18:03
 */

public class ClientBootStrap {
    //维护消费者的map
    public static ConcurrentHashMap<String,Object> consumers = new ConcurrentHashMap<>();

    //上下文
    private Configuration configuration = new Configuration();

    //消费者配置文件路径
    private static final String PROPERTIES_PATH = ClientBootStrap.class.getResource("/").getPath() + "/consumerconfig";



    public ClientBootStrap(){
        this(100);
    }

    public ClientBootStrap(int threads){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threads,
                threads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
        configuration.setServiceThreadPool(threadPoolExecutor);
    }

    //解析配置文件 生成消费者
    public  void initClient() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        File folder = new File(PROPERTIES_PATH);
        if (!folder.exists()){
            throw new FileNotFoundException("配置文件目录" + PROPERTIES_PATH + "不存在");
        }

        if (!folder.isDirectory()){
            throw new RuntimeException(PROPERTIES_PATH + "不是目录");
        }

        File[] files = folder.listFiles();
        for (File file:files){
            if (file.isDirectory())
                continue;

            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            //哪个各个配置
            String consumerInterface = properties.getProperty("consumer.interface");
            String consumerId = properties.getProperty("consumer.id");
            String providerId = properties.getProperty("provider.id");
            String host = properties.getProperty("provider.host");
            String port = properties.getProperty("provider.port");

            //这个对象时用于构造url和创建客户端连接
            ConsumerConfigEntity entity = new ConsumerConfigEntity();
            entity.setProviderHost(host);
            entity.setProviderId(providerId);
            entity.setProviderPort(Integer.valueOf(port));

            //用消费者接口的类型的全类名获取对应的CLass对象
            Class<?> aClass = Class.forName(consumerInterface);

            //创建一个Stub对象
            ClientStub clientStub = new ClientStub(aClass, configuration,entity);

            //获取代理对象，真正执行调用的是这对象
            Object stub = clientStub.getStub();

            //添加到消费者集合中，key是消费者ID。
            consumers.put(consumerId,stub);
        }
    }

    public Object getConsumer(String consumerId){
        return consumers.get(consumerId);
    }
}
