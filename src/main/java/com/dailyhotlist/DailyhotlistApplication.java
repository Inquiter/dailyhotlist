package com.dailyhotlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DailyhotlistApplication {
    public static void main(String[] args) {
        SpringApplication.run(DailyhotlistApplication.class, args);
    }
}
