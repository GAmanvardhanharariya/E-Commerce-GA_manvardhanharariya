package com.ecommerce.demo.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class DotenvLoader {
    private DotenvLoader() {}

    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e -> {
            // Put .env values into JVM system properties so Spring can read ${...}
            if (System.getProperty(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
            }
        });
    }
}
