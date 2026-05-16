package in.lekhai.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(
               "ewb"  // bucket to store ewaybill related objects
        );
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        // TODO: replace with actual cache, right now 5Hrs since taxPro token is valid for 6Hrs
        return Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofHours(5))
                .recordStats();
    }
}
