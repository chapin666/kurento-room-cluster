package tv.lycam.sdk;

import com.hazelcast.config.Config;

/**
 * Created by lycamandroid on 2017/6/12.
 */
public class HazelcastConfiguration {

    // When Spring Boot find a com.hazelcast.config.Config
    // automatically instantiate a HazelcastInstance
    public static Config config() {
       Config config = new Config();
       config.setInstanceName("sdk");
       return config;
    }



}
