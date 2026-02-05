package com.ecommerce.demo.config;

import com.ecommerce.demo.product.entity.Product;
import com.ecommerce.demo.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadProducts(ProductRepository productRepository) {
        return args -> {

            // Avoid duplicate inserts on restart
            if (productRepository.count() > 0) {
                return;
            }

            List<Product> products = List.of(
                    Product.builder()
                            .name("iPhone 13")
                            .description("Apple iPhone 13 128GB")
                            .price(new BigDecimal("49999.99"))
                            .stock(15)
                            .active(true)
                            .build(),

                    Product.builder()
                            .name("Samsung Galaxy S22")
                            .description("Samsung flagship smartphone")
                            .price(new BigDecimal("42999.00"))
                            .stock(20)
                            .active(true)
                            .build(),

                    Product.builder()
                            .name("MacBook Air M1")
                            .description("Apple MacBook Air with M1 chip")
                            .price(new BigDecimal("89999.99"))
                            .stock(8)
                            .active(true)
                            .build(),

                    Product.builder()
                            .name("Dell XPS 13")
                            .description("Premium ultrabook from Dell")
                            .price(new BigDecimal("99999.00"))
                            .stock(5)
                            .active(true)
                            .build(),

                    Product.builder()
                            .name("Sony WH-1000XM5")
                            .description("Noise cancelling headphones")
                            .price(new BigDecimal("29999.00"))
                            .stock(25)
                            .active(true)
                            .build()
            );

            productRepository.saveAll(products);

            System.out.println("Sample products inserted into database");
        };
    }
}
