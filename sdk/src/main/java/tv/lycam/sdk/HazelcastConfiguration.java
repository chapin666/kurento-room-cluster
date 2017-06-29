package tv.lycam.sdk;

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

    private static Config config;

    // When Spring Boot find a com.hazelcast.config.Config
    // automatically instantiate a HazelcastInstance
    public static Config config() {
        if (config == null) {
            config = new Config();
            config.setInstanceName("sdk");
        }
        return config;
    }


}
