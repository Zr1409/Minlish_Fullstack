package com.minlish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MinlishServersApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinlishServersApplication.class, args);
    }
}
