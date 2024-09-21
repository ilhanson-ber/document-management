package com.ilhanson.document_management;

import com.ilhanson.document_management.services.AuthorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DocumentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagementApplication.class, args);
    }

    @Bean
    CommandLineRunner run(AuthorService authorService) {
        return args -> {
            authorService.tempTest();
        };

    }
}
