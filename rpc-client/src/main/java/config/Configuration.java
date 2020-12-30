package config;

import handler.RpcClientServiceHandler;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 11:17
 */

@Data
public class Configuration {

    private ThreadPoolExecutor serviceThreadPool;

    //RpcClientServiceHandler map   key为  host:port  比如 127.0.0.1:8899 值为RpcClientServiceHandler数组
    private Map<String,RpcClientServiceHandler> rpcClientServiceHandlerMap = new ConcurrentHashMap<>();
}
