package tv.lycam.server.api.module.user;

import java.io.Serializable;

/**
 * Created by lycamandroid on 2017/6/21.
 */
public class SMSModel implements Serializable {

    private String code;

    public SMSModel(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
