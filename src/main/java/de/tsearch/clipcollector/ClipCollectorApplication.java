package de.tsearch.clipcollector;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClipCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClipCollectorApplication.class, args);
    }

    @Bean
    CommandLineRunner afterStart() {
        return args -> {
        };
    }
}
