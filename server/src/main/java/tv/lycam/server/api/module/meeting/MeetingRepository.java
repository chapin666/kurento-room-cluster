package tv.lycam.server.api.module.meeting;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lycamandroid on 2017/6/19.
 */
@Repository
public interface MeetingRepository extends MongoRepository<MeetingModel, String> {

}
