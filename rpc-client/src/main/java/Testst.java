import api.HelloService;
import bootstrap.ClientBootStrap;
import config.Configuration;
import config.ConsumerConfigEntity;
import stub.ClientStub;

import java.io.IOException;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 18:07
 */

public class Testst {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        ClientBootStrap clientBootStrap = new ClientBootStrap();
        clientBootStrap.initClient();
        HelloService helloServiceConsumer = (HelloService) clientBootStrap.getConsumer("HelloServiceConsumer");
        new Thread(()->{
            System.out.println(helloServiceConsumer.sayHallo("Yehaocong"));
        }).start();
        new Thread(()->{
            System.out.println(helloServiceConsumer.sayHallo("Yehaoxian"));
        }).start();
        System.out.println(helloServiceConsumer.sayHallo("Yehaoxian"));

    }
}
