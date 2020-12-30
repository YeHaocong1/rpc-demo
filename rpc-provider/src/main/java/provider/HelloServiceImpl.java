package provider;

import api.HelloService;

import java.util.concurrent.TimeUnit;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/28 18:55
 */

public class HelloServiceImpl implements HelloService {
    public String sayHallo(String name) {
        if (name.equals("Yehaocong")) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "Hello," + name;
    }
}
