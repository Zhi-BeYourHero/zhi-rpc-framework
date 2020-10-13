package com.zhi.registry;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class ZkServiceRegistryTest {
    @Test
    void should_register_service_successful_and_lookup_service_by_service_name() {
        ServiceRegistry zkServiceRegistry = new ZkServiceRegistry();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        zkServiceRegistry.registerService("com.zhi.registry.ZkServiceRegistry", givenInetSocketAddress);
        ServiceDiscovery serviceDiscovery = new ZkServiceDiscovery();
        InetSocketAddress acquiredInetSocketAddress = serviceDiscovery.lookupService("com.zhi.registry.ZkServiceRegistry");
        assertEquals(givenInetSocketAddress.toString(), acquiredInetSocketAddress.toString());
    }
}