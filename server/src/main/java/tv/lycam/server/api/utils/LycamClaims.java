package tv.lycam.server.api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.RequiredTypeException;
import io.jsonwebtoken.impl.JwtMap;

import java.util.Date;
import java.util.Map;

/**
 * Created by lycamandroid on 2017/6/29.
 */
public class LycamClaims  extends JwtMap implements Claims {

    public LycamClaims() {
    }

    public LycamClaims(Map<String, Object> map) {
        super(map);
    }

    public String getIssuer() {
        return this.getString("iss");
    }

    public Claims setIssuer(String iss) {
        this.setValue("iss", iss);
        return this;
    }

    public String getSubject() {
        return this.getString("sub");
    }

    public Claims setSubject(String sub) {
        this.setValue("sub", sub);
        return this;
    }

    public String getAudience() {
        return this.getString("aud");
    }

    public Claims setAudience(String aud) {
        this.setValue("aud", aud);
        return this;
    }

    public Date getExpiration() {
        return (Date)this.get("exp", Date.class);
    }

    public Claims setExpiration(Date exp) {
        this.setDate("exp", exp);
        return this;
    }

    public Date getNotBefore() {
        return (Date)this.get("nbf", Date.class);
    }

    public Claims setNotBefore(Date nbf) {
        this.setDate("nbf", nbf);
        return this;
    }

    public Date getIssuedAt() {
        return (Date)this.get("iat", Date.class);
    }

    public Claims setIssuedAt(Date iat) {
        this.setDate("iat", iat);
        return this;
    }

    public String getId() {
        return this.getString("jti");
    }

    public Claims setId(String jti) {
        this.setValue("jti", jti);
        return this;
    }

    public <T> T get(String claimName, Class<T> requiredType) {
        Object value = this.get(claimName);
        if(value == null) {
            return null;
        } else {
            if("exp".equals(claimName) || "iat".equals(claimName) || "nbf".equals(claimName)) {
                value = this.getDate(claimName);
            }

            if(requiredType == Date.class && value instanceof Long) {
                value = new Date(((Long)value).longValue());
            }

            if(!requiredType.isInstance(value)) {
                throw new RequiredTypeException("Expected value to be of type: " + requiredType + ", but was " + value.getClass());
            } else {
                return requiredType.cast(value);
            }
        }
    }
}
