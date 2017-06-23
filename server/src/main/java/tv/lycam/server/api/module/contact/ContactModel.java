package tv.lycam.server.api.module.contact;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * Created by lycamandroid on 2017/6/19.
 */
public class ContactModel implements Serializable {

    @Id
    private String id;

    private String username;

    private String phone;

    private String createTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
