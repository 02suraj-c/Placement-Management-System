package com.placement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlacementManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlacementManagementApplication.class, args);
    }
}
