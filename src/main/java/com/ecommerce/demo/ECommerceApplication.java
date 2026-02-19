package com.ecommerce.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ecommerce.demo.config.DotenvLoader;


@SpringBootApplication
public class ECommerceApplication {

	public static void main(String[] args) {
		DotenvLoader.load();
		SpringApplication.run(ECommerceApplication.class, args);
	}

}
