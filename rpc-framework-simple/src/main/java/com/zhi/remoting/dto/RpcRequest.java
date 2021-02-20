package com.zhi.remoting.dto;

import com.zhi.entity.RpcServiceProperties;
import com.zhi.remoting.model.ProviderService;
import lombok.*;
import java.io.Serializable;

/**
 * @Description 自定义消息传输体
 * v[2.0]从原来的导入@Data改为指定需要的，估计是避免不必要的方法吧
 * @Author WenZhiLuo
 * @Date 2020-10-10 10:12
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private ProviderService providerService;
    // 请求ID，唯一标识一次返回值
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;
    //消费端应用名
    private String appName;
    //消费请求超时时长
    private long invokeTimeout;
    private String failMode;
    public RpcServiceProperties toRpcProperties() {
        return RpcServiceProperties.builder().serviceName(this.getInterfaceName())
                .version(this.getVersion())
                .group(this.getGroup()).build();
    }
}