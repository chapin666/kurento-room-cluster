package tv.lycam.server.api.module.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lycamandroid on 2017/6/19.
 */
@Repository
public interface UserRepository extends MongoRepository<UserModel, String> {


    public UserModel findByPhone(String phone);

    public UserModel findByPhoneAndPassword(String phone, String password);

}
