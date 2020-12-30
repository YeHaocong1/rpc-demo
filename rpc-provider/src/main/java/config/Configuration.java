package config;

import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/28 20:27
 */

@Data
public class Configuration {

    //用于业务逻辑的线程池，使得Netty的线程专注于IO
    private ThreadPoolExecutor serviceExecutor;

    //注册的服务提供者集合
    private ConcurrentHashMap<String,Object> providers;
}
