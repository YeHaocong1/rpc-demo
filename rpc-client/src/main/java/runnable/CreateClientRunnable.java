package runnable;

import config.Configuration;
import rpcclient.RpcClient;
import stub.ClientStub;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 18:30
 */

public class CreateClientRunnable  implements Runnable{
    private ClientStub clientStub;

    private String host;

    private int port;

    private Configuration configuration;

    public CreateClientRunnable(ClientStub clientStub,String host,int port,Configuration configuration){
        this.clientStub = clientStub;
        this.host = host;
        this.port = port;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        //创建
            RpcClient rpcClient = new RpcClient(host, port, configuration);
            try {
                //启动
                rpcClient.start(clientStub);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }
}
