package edu.kit.datamanager.rocraterest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RoCrateRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoCrateRestApplication.class, args);
    }

    @GetMapping("/health")
    public String health(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s :D!!!!!!", name);
    }
}
