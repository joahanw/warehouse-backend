package com.johanwork.warehouse.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Bean
    public CacheManager caffeineCacheManager(){
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                cache("category-list", 50, 360),
                cache("categories", 50, 360),
                cache("category-response", 50, 360),

                cache("role-list", 20, 360),
                cache("roles", 20, 360),

                cache("product-list", 1000, 60),
                cache("products", 1000, 120),

                cache("user-list", 1000, 60),
                cache("users", 1000, 120),

                cache("warehouse-list", 50, 180),
                cache("warehouses", 50, 360),

                cache("warehouse-product-list", 1000, 60),
                cache("warehouses-product", 1000, 120),
                cache("warehouses-product-by-warehouse-id", 1000, 120),
                cache("warehouses-product-by-warehouse-id-and-product-id", 1000, 120),

                cache("merchant-list", 50, 180),
                cache("merchants", 50, 360),

                cache("merchant-product-list", 1000, 60),
                cache("merchant-product", 1000, 120)
        ));
        return cacheManager;
    }

    private CaffeineCache cache(
            String name,
            long size,
            long minutes
    ){
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .initialCapacity(10)
                        .maximumSize(size)
                        .expireAfterWrite(minutes, TimeUnit.MINUTES)
                        .recordStats()
                        .build()
        );
    }

}
