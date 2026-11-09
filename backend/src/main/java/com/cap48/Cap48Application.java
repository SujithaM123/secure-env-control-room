package com.cap48;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Cap48Application {

    public static void main(String[] args) {
        SpringApplication.run(Cap48Application.class, args);
    }
}
