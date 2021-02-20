package com.zhi.utils.ip;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-18 20:31
 */
@SuppressWarnings("checkstyle:LocalVariableName")
@Slf4j
public class IPUtil {

    private static String hostIp;

    /**
     * 获取本机Ip
     * <p/>
     * 通过获取系统所有的networkInterface网络接口 然后遍历 每个网络下的InterfaceAddress组。
     * 获得符合 <code>InetAddress instanceof Inet4Address</code> 条件的一个IpV4地址
     * @return
     */
    public static String localIp() {
        return hostIp;
    }


    public static String getRealIp() {
        // 本地IP，如果没有配置外网IP则返回它
        String localip = null;
        // 外网IP
        String netip = null;

        try {
            Enumeration<NetworkInterface> netInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            // 是否找到外网IP
            boolean finded = false;
            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    // 外网IP
                    if (!ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) {
                        netip = ip.getHostAddress();
                        finded = true;
                        break;
                        // 内网IP
                    } else if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) {
                        localip = ip.getHostAddress();
                    }
                }
            }
            if (netip != null && !"".equals(netip)) {
                return netip;
            } else {
                return localip;
            }
        } catch (SocketException e) {
            log.warn("获取本机Ip失败:异常信息:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    static {
        String ip = null;
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                List<InterfaceAddress> InterfaceAddress = netInterface.getInterfaceAddresses();
                for (InterfaceAddress add : InterfaceAddress) {
                    InetAddress Ip = add.getAddress();
                    if (Ip != null && Ip instanceof Inet4Address) {
                        if (StringUtils.equals(Ip.getHostAddress(), "127.0.0.1")) {
                            continue;
                        }
                        ip = Ip.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("获取本机Ip失败:异常信息:" + e.getMessage());
            throw new RuntimeException(e);
        }
        hostIp = ip;
    }


    /**
     * 获取主机第一个有效ip<br/>
     * 如果没有效ip，返回空串
     *
     * @return
     */
    public static String getHostFirstIp() {
        return hostIp;
    }

    public static void main(String[] args) throws Exception {
        //System.out.println(localIp());
        System.out.println(getRealIp());
        System.out.println(getHostFirstIp());
    }
}
