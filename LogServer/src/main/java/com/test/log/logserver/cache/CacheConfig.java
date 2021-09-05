package com.test.log.logserver.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {
    public static final String LOG = "Log";
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(LOG);

        return cacheManager;
    }

    @CacheEvict(allEntries = true, value = {LOG})
    @Scheduled(fixedDelayString = "${cache.expired.time}" ,  initialDelay = 500)
    public void reportCacheEvict() {
        System.out.println("Flush Cache !");
    }
}