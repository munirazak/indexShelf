package com.collabera.librarysystem;

import com.collabera.librarysystem.config.SchemaBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibrarySystemApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(LibrarySystemApplication.class);
        application.addListeners(new SchemaBootstrap());
        application.run(args);
    }
}
