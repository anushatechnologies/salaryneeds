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
            if (categoryRepository.count() == 0) {
                categoryRepository.save(Category.builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .name("Home Cleaning")
                        .description("Full house, kitchen, and bathroom deep cleaning services")
                        .iconUrl("https://images.unsplash.com/photo-1581578731548-c64695cc6952")
                        .displayOrder(1)
                        .isActive(true)
                        .build());
                categoryRepository.save(Category.builder()
                        .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .name("Plumbing")
                        .description("Pipe repairs, leak fixing, tap installation, and drainage")
                        .iconUrl("https://images.unsplash.com/photo-1607472586893-edb57bdc0e39")
                        .displayOrder(2)
                        .isActive(true)
                        .build());
                categoryRepository.save(Category.builder()
                        .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                        .name("Electrical")
                        .description("Wiring, switch repair, appliance setup, and short-circuit repair")
                        .iconUrl("https://images.unsplash.com/photo-1621905251189-08b45d6a269e")
                        .displayOrder(3)
                        .isActive(true)
                        .build());
                categoryRepository.save(Category.builder()
                        .id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                        .name("Appliance Repair")
                        .description("AC servicing, refrigerator repair, washing machine repair")
                        .iconUrl("https://images.unsplash.com/photo-1581092160607-ee22621dd758")
                        .displayOrder(4)
                        .isActive(true)
                        .build());
                categoryRepository.save(Category.builder()
                        .id(UUID.fromString("55555555-5555-5555-5555-555555555555"))
                        .name("Salon for Women")
                        .description("Facial, manicure, pedicure, hair spa at doorstep")
                        .iconUrl("https://images.unsplash.com/photo-1560066984-138dadb4c035")
                        .displayOrder(5)
                        .isActive(true)
                        .build());

                System.out.println("=============================================");
                System.out.println("Initialized Seed Categories for SalaryNeeds!");
                System.out.println("=============================================");
            }
        };
    }
}
