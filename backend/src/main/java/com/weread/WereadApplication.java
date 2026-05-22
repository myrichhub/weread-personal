package com.weread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WereadApplication {
    public static void main(String[] args) {
        SpringApplication.run(WereadApplication.class, args);
    }
}
