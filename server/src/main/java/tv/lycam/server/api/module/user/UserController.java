package tv.lycam.server.api.module.user;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tv.lycam.server.api.response.ResponseModel;
import tv.lycam.server.api.storage.StorageService;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by lycamandroid on 2017/6/19.
 */

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CCPRestSmsSDK ccpRestSmsSDK;


    private final StorageService storageService;



    @Autowired
    public UserController(StorageService storageService) {
        this.storageService = storageService;
    }


    @RequestMapping("/send_sms/{phone}")
    public ResponseEntity smsSend(@PathVariable("phone")String phone) {
        HashMap<String, Object> result = this.ccpRestSmsSDK
                .sendTemplateSMS(phone, "184334", new String[]{"1102", "2分钟"});

        System.out.println("SDKTestGetSubAccounts result=" + result);

        if("000000".equals(result.get("statusCode"))){
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
        }else{
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }

        SMSModel smsModel = new SMSModel("1102");

        return ResponseEntity.ok(new ResponseModel<>(smsModel));
    }

    /**
     *
     * @param userModel
     * @return
     */
    @PostMapping(value = "/register")
    @ResponseBody
    public ResponseEntity register(@RequestParam(value = "file", required = false) MultipartFile file,
                               @Validated @RequestBody UserModel userModel,
                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return sendValidateError(bindingResult);
        }

        // upload avatar
        String fileName = null;
        if (file != null) {
            storageService.store(file);
            fileName = file.getName();
        }

        try {
            userModel.setAvatar(fileName);
            UserModel model = userRepository.save(userModel);
            if (model == null) {
                return ResponseEntity.ok("注册失败");
            }
            return ResponseEntity.ok(model);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("注册失败");
        }
    }


    /**
     *
     * @param userModel
     * @param bindingResult
     * @return
     */
    @PostMapping(value="/login")
    @ResponseBody
    public ResponseEntity login(@Validated @RequestBody UserModel userModel, BindingResult bindingResult) {

        // validate
        if (bindingResult.hasErrors()) {
           return sendValidateError(bindingResult);
        }

        String phone = userModel.getPhone();
        String password = userModel.getPassword();

        UserModel model = userRepository.findByPhone(phone);
        if (model == null) {
            return ResponseEntity.ok(new ResponseModel<>("该手机号不存在"));
        }

        if (!password.equals(model.getPassword())) {
            return ResponseEntity.ok(new ResponseModel<>("密码错误"));
        }

        return ResponseEntity.ok(new ResponseModel<>(model));
    }


    private ResponseEntity sendValidateError(BindingResult bindingResult) {
        String err = bindingResult.getFieldError().getDefaultMessage();
        return ResponseEntity.ok(new ResponseModel<>(err));
    }


}
