package config;

import lombok.Data;

/**
 * @author YeHaocong
 * @decription TODO
 * @Date 2020/12/29 11:58
 */

@Data
public class ConsumerConfigEntity {

    private String providerId;

    private  String providerHost;

    private int providerPort;
}
