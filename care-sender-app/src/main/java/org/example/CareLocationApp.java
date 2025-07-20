package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"org.example", "rs.ac.uns.ftn.informatika"})
public class CareLocationApp {

    public static void main(String[] args) {
        SpringApplication.run(CareLocationApp.class, args);
    }
}