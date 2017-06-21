package tv.lycam.server.api.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by lycamandroid on 2017/6/21.
 */
@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "upload-dir";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}