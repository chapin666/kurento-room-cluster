package tv.lycam.server.api.module.meeting;

import io.netty.handler.codec.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tv.lycam.server.api.response.ResponseModel;

import java.util.List;

/**
 * Created by lycamandroid on 2017/6/19.
 */

@RestController
@RequestMapping("/meeting")
public class MeetingController {


    @Autowired
    private MeetingRepository meetingRepository;

    /**
     *
     * @param model
     * @return
     */
    @PostMapping
    public ResponseEntity createMeeting(@RequestBody MeetingModel model) {
        MeetingModel meetingModel = meetingRepository.insert(model);
        return ResponseEntity.ok(new ResponseModel<>(meetingModel));
    }

    /**
     *
     * @param model
     * @return
     */
    @PutMapping
    public ResponseEntity putMeeting(@RequestBody MeetingModel model) {
        MeetingModel meetingModel = meetingRepository.save(model);
        return ResponseEntity.ok(new ResponseModel<>(meetingModel));
    }


    /**
     *
     * @return
     */
    @GetMapping
    public ResponseEntity meetings() {
        List<MeetingModel> meetingModelList = meetingRepository.findAll();
        return ResponseEntity.ok(new ResponseModel<>(meetingModelList));
    }


    @DeleteMapping("/{meetingId}")
    public ResponseEntity delMeeting(@PathVariable String meetingId) {

        try {
            meetingRepository.delete(meetingId);
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseModel<>("删除会议失败"));
        }

        return ResponseEntity.ok(new ResponseModel<>(true, null, "删除会议成功"));
    }

}
