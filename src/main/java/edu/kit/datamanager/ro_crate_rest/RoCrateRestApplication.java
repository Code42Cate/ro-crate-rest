package edu.kit.datamanager.ro_crate_rest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.kit.datamanager.ro_crate.RoCrate;

@SpringBootApplication
@RestController
public class RoCrateRestApplication {

  public static void main(String[] args) {

    RoCrate crate = new RoCrate.RoCrateBuilder().build();

    System.out.println(crate.getJsonMetadata().toString());
    System.out.println(crate.getAllDataEntities().get(0).getId());

    SpringApplication.run(RoCrateRestApplication.class, args);
  }

  @Bean
  public OpenAPI customOpenAPI() {
    // TODO: add more info to the OpenAPI
    return new OpenAPI()
        .info(
            new Info()
                .title("RoCrate REST API")
                .description("REST API for RoCrate")
                .version("0.0.1")
                .license(new License().name("Apache 2.0").url("http://springdoc.org")));
  }

  @GetMapping("/health")
  public String health(
      @RequestParam(value = "name", defaultValue = "World") String name) {
    return String.format("Hello %s :D!!!!!!", name);
  }

}
