package tv.lycam;

import com.hazelcast.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lycamandroid on 2017/6/12.
 */
@Configuration
public class HazelcastConfiguration {

    // When Spring Boot find a com.hazelcast.config.Config
    // automatically instantiate a HazelcastInstance
    @Bean
    public Config config() {
        return new Config();
    }



}
