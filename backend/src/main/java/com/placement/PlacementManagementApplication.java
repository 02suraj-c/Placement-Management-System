package com.placement;

import com.placement.ai.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class PlacementManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlacementManagementApplication.class, args);
    }
}
