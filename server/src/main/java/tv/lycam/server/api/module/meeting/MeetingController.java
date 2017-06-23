package tv.lycam.server.api.module.meeting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.lycam.server.api.module.comm.BaseController;
import tv.lycam.server.api.response.ResponseModel;

import java.util.List;

/**
 * Created by lycamandroid on 2017/6/19.
 */

@RestController
@RequestMapping("/meeting")
public class MeetingController extends BaseController {


    @Autowired
    private MeetingRepository meetingRepository;

    /**
     *
     * @param model
     * @return
     */
    @PostMapping
    public ResponseEntity createMeeting(
            @RequestBody @Validated MeetingModel model,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return sendValidateError(bindingResult);
        }
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



    @GetMapping("/{meetingId}")
    public ResponseEntity find(@PathVariable String meetingId) {

        MeetingModel meetingModel = meetingRepository.findOne(meetingId);

        if (meetingModel != null) {
            return ResponseEntity.ok(new ResponseModel<>(meetingModel));
        } else {
            return ResponseEntity.ok(new ResponseModel<>("没有查询到该会议"));
        }

    }


    /**
     *
     * @return
     */
    @GetMapping
    public ResponseEntity list() {
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
