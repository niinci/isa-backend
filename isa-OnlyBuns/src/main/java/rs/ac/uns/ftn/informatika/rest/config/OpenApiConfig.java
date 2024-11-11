package rs.ac.uns.ftn.informatika.rest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/*
konfiguracija omogucava generisanje metapodataka API-ju, poput vlasnika, uslova koriscenja, licence i sl.
moguce je konfigurisati i vise servera
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OnlyBunsAPI")
                        .version("v1")
                        .description("API documentation with Swagger"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")));
    }

    /*@Bean
    public OpenAPI openApi() {
        ArrayList<Server> servers = new ArrayList<>(3);
        servers.add(new Server().url("http://localhost:8080/").description("development server"));
        servers.add(new Server().url("http://qa:8081/").description("test server"));
        servers.add(new Server().url("http://www.rest-example.com/").description("production server"));

        return new OpenAPI()
                .info(new Info()
                        .title("OnlyBans")
                        .description("Rest example with OpenAPI documentation")
                        .version("v1.0")
                        .contact(new Contact()      // kontakt informacije
                                .name("FTN")
                                .url("https://github.com/stojkovm")     // mora biti u url formatu
                                .email("ftn@uns.ac.rs"))
                        .termsOfService("TOC")
                        .license(new License().name("License").url("#"))
                    )
                .servers(servers);
    }*/

}
