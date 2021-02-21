package com.zhi.enums;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 13:29
 */
public enum RpcConfigPropertiesEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address"),
    PORT("port");
    private final String propertyValue;
    RpcConfigPropertiesEnum(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}