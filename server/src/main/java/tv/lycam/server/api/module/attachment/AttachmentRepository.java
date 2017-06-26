package tv.lycam.server.api.module.attachment;

import org.springframework.data.mongodb.repository.MongoRepository;
import tv.lycam.server.api.module.contact.ContactModel;

import java.util.List;

/**
 * Created by lycamandroid on 2017/6/19.
 */
public interface AttachmentRepository extends MongoRepository<AttachmentModel, String>  {

    public List<AttachmentModel> findByCreateUserId(String id);

}
