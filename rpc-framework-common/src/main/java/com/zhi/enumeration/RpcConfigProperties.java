package com.zhi.enumeration;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2020-10-27 13:29
 */
public enum RpcConfigProperties {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");
    private final String propertyValue;
    RpcConfigProperties(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}