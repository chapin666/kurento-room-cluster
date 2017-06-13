package tv.lycam;

import com.hazelcast.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by lycamandroid on 2017/6/12.
 */
public class HazelcastConfiguration {

    // When Spring Boot find a com.hazelcast.config.Config
    // automatically instantiate a HazelcastInstance
    public static Config config() {
       Config config = new Config();
       config.setInstanceName("server");
       return config;
    }



}
