package tv.lycam.server.api.module.contact;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lycamandroid on 2017/6/19.
 */
@Repository
public interface ContactRepository extends MongoRepository<ContactModel, String> {

}
