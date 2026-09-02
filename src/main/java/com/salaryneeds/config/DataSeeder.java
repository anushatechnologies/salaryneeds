package com.salaryneeds.config;

import com.salaryneeds.entity.Category;
import com.salaryneeds.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepository) {
        return args -> {
            categoryRepository.save(new Category(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Home Cleaning", "Home Cleaning Services"));
            categoryRepository.save(new Category(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Plumbing", "Plumbing Services"));
            categoryRepository.save(new Category(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Childcare", "Childcare Services"));
            categoryRepository.save(new Category(UUID.fromString("44444444-4444-4444-4444-444444444444"), "Loading & Unloading", "Loading and Unloading Services"));
            categoryRepository.save(new Category(UUID.fromString("55555555-5555-5555-5555-555555555555"), "Cooking", "Cooking Services"));

            System.out.println("=============================================");
            System.out.println("Inserted 5 Main Categories for testing!");
            System.out.println("Home Cleaning ID: 11111111-1111-1111-1111-111111111111");
            System.out.println("Plumbing ID: 22222222-2222-2222-2222-222222222222");
            System.out.println("Childcare ID: 33333333-3333-3333-3333-333333333333");
            System.out.println("Loading & Unloading ID: 44444444-4444-4444-4444-444444444444");
            System.out.println("Cooking ID: 55555555-5555-5555-5555-555555555555");
            System.out.println("=============================================");
        };
    }
}
