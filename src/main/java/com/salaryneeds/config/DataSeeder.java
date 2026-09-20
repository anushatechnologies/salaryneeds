package com.salaryneeds.config;

import com.salaryneeds.entity.Category;
import com.salaryneeds.entity.ServiceItem;
import com.salaryneeds.repository.CategoryRepository;
import com.salaryneeds.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            Category cat1 = categoryRepository.save(Category.builder()
                    .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .name("Home Cleaning")
                    .description("Full house, kitchen, and bathroom deep cleaning services")
                    .iconUrl("https://images.unsplash.com/photo-1581578731548-c64695cc6952")
                    .displayOrder(1)
                    .isActive(true)
                    .build());
            Category cat2 = categoryRepository.save(Category.builder()
                    .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                    .name("Plumbing")
                    .description("Pipe repairs, leak fixing, tap installation, and drainage")
                    .iconUrl("https://images.unsplash.com/photo-1607472586893-edb57bdc0e39")
                    .displayOrder(2)
                    .isActive(true)
                    .build());
            Category cat3 = categoryRepository.save(Category.builder()
                    .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                    .name("Electrical")
                    .description("Wiring, switch repair, appliance setup, and short-circuit repair")
                    .iconUrl("https://images.unsplash.com/photo-1621905251189-08b45d6a269e")
                    .displayOrder(3)
                    .isActive(true)
                    .build());
            Category cat4 = categoryRepository.save(Category.builder()
                    .id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                    .name("Appliance Repair")
                    .description("AC servicing, refrigerator repair, washing machine repair")
                    .iconUrl("https://images.unsplash.com/photo-1581092160607-ee22621dd758")
                    .displayOrder(4)
                    .isActive(true)
                    .build());
            Category cat5 = categoryRepository.save(Category.builder()
                    .id(UUID.fromString("55555555-5555-5555-5555-555555555555"))
                    .name("Salon for Women")
                    .description("Facial, manicure, pedicure, hair spa at doorstep")
                    .iconUrl("https://images.unsplash.com/photo-1560066984-138dadb4c035")
                    .displayOrder(5)
                    .isActive(true)
                    .build());

            // Seed sample active subcategories / services
            serviceItemRepository.save(ServiceItem.builder()
                    .category(cat1)
                    .name("Full House Deep Cleaning")
                    .description("Thorough deep cleaning of 2BHK/3BHK including floor scrubbing")
                    .basePrice(new BigDecimal("1499.00"))
                    .isActive(true)
                    .build());
            serviceItemRepository.save(ServiceItem.builder()
                    .category(cat1)
                    .name("Kitchen Deep Cleaning")
                    .description("Oil and grease removal from cabinets, slabs, and exhaust")
                    .basePrice(new BigDecimal("799.00"))
                    .isActive(true)
                    .build());
            serviceItemRepository.save(ServiceItem.builder()
                    .category(cat2)
                    .name("Tap & Mixer Repair")
                    .description("Leaking tap replacement and mixer installation")
                    .basePrice(new BigDecimal("199.00"))
                    .isActive(true)
                    .build());
            serviceItemRepository.save(ServiceItem.builder()
                    .category(cat3)
                    .name("Switchboard Repair")
                    .description("Fix sparking switches, socket replacement, MCB repair")
                    .basePrice(new BigDecimal("149.00"))
                    .isActive(true)
                    .build());
            serviceItemRepository.save(ServiceItem.builder()
                    .category(cat4)
                    .name("AC Foam Jet Service")
                    .description("Deep AC cooling coil cleaning with indoor unit wash")
                    .basePrice(new BigDecimal("599.00"))
                    .isActive(true)
                    .build());

            log.info("Initialized Seed Categories and Services for SalaryNeeds!");
        }
    }
}
