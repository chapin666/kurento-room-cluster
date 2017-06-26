package tv.lycam.server.api.module.attachment;

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
@RequestMapping("/attachment")
public class AttachmentController extends BaseController {

    @Autowired
    private AttachmentRepository attachmentRepository;


    /**
     *
     * @param model
     * @return
     */
    @PostMapping
    public ResponseEntity post(@Validated @RequestBody AttachmentModel model, BindingResult buildResult) {

        if (buildResult.hasErrors()) {
            return sendValidateError(buildResult);
        }


        AttachmentModel attachmentModel = attachmentRepository.insert(model);

        if (attachmentModel == null) {
            return ResponseEntity.ok(new ResponseModel<>("保存文件错误"));
        }

        return ResponseEntity.ok(new ResponseModel<>(attachmentModel));
    }




    @GetMapping
    public ResponseEntity list() {
        List<AttachmentModel> list = attachmentRepository.findAll();
        return ResponseEntity.ok(new ResponseModel<>(list));
    }


    @GetMapping("/{user_id}")
    public ResponseEntity listByUser(@PathVariable("user_id") String userId) {
        List<AttachmentModel> list =  attachmentRepository.findByCreateUserId(userId);
        return ResponseEntity.ok(new ResponseModel<>(list));
    }


}
