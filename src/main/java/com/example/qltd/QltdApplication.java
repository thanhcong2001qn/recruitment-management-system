package com.example.qltd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QltdApplication {

    public static void main(String[] args) {
        SpringApplication.run(QltdApplication.class, args);
    }

}
