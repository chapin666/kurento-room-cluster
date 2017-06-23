package tv.lycam.server.api.module.user;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tv.lycam.sdk.HazelcastConfiguration;
import tv.lycam.server.RoomJsonRpcHandler;
import tv.lycam.server.api.module.comm.BaseController;
import tv.lycam.server.api.response.ResponseModel;
import tv.lycam.server.api.storage.StorageService;
import tv.lycam.server.api.utils.RandomUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by lycamandroid on 2017/6/19.
 */

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    private  static final Logger log = LoggerFactory.getLogger(RoomJsonRpcHandler.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CCPRestSmsSDK ccpRestSmsSDK;

    private final StorageService storageService;

    private final HazelcastInstance hazelcastInstance;

    private final ConcurrentMap<String, SMSModel> SMSDB;



    @Autowired
    public UserController(StorageService storageService) {
        this.storageService = storageService;
        this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(HazelcastConfiguration.config());
        this.SMSDB = this.hazelcastInstance.getMap("sms");
    }


    @RequestMapping("/send_sms/{phone}")
    public ResponseEntity smsSend(@PathVariable("phone")String phone) {

        String number = RandomUtils.getRandNum(6);

        HashMap<String, Object> result = this.ccpRestSmsSDK.sendTemplateSMS(phone, "184334", new String[]{number, "2分钟"});


        if("000000".equals(result.get("statusCode"))){
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
        }else{
            return ResponseEntity.ok(new ResponseModel<>(result.get("statusMsg").toString()));
        }

        SMSModel smsModel = new SMSModel(number);

        this.SMSDB.put(phone, smsModel);

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


        String phone = userModel.getPhone();
        String code = userModel.getCode();

        UserModel m = userRepository.findByPhone(phone);

        if (m != null) {
            return ResponseEntity.ok(new ResponseModel<>("该手机号已注册"));
        }


        SMSModel sms = this.SMSDB.get(phone);
        if (sms == null || code == null) {
            return ResponseEntity.ok(new ResponseModel<>("请获取验证码"));
        }

        if (!code.equals(sms.getCode())) {
            return ResponseEntity.ok(new ResponseModel<>("验证码错误"));
        }

        // upload avatar
        String fileName = null;
        if (file != null) {
            storageService.store(file);
            fileName = file.getName();
        }

        try {
            userModel.setAvatar(fileName);
            UserModel model = userRepository.insert(userModel);
            if (model == null) {
                return ResponseEntity.ok("注册失败");
            }
            return ResponseEntity.ok(model);
        } catch (Exception e) {
            log.error(e.getMessage());
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

        String jwtToken = "";

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

        System.out.println(model.getPassword());

        if (!password.equals(model.getPassword())) {
            return ResponseEntity.ok(new ResponseModel<>("密码错误"));
        }

        jwtToken = Jwts.builder().setSubject(phone).claim("roles", "user").setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "secretkey").compact();

        return ResponseEntity.ok(new ResponseModel<>(true, null, jwtToken));
    }


}
