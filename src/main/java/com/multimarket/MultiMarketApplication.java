package com.multimarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(excludeName = {
        "org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration",
        "org.springframework.cloud.autoconfigure.RefreshAutoConfiguration",
        "org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration",
        "org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration"
})
@EnableAsync
public class MultiMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiMarketApplication.class, args);
	}

}
