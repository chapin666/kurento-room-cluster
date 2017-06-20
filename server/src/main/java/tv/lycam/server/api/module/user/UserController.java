package tv.lycam.server.api.module.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.lycam.server.api.response.ResponseModel;

/**
 * Created by lycamandroid on 2017/6/19.
 */

@RestController
@RequestMapping("/user")
public class UserController {


    //https://www.mkyong.com/spring-mvc/spring-rest-api-validation/

    @Autowired
    private UserRepository userRepository;


    /**
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/register")
    @ResponseBody
    public ResponseModel<UserModel> post(@Validated @RequestBody UserModel user, Errors errors) {

        if (errors.hasErrors()) {
            return new ResponseModel<UserModel>("用户名或者密码不能为空");
        }

        UserModel model = userRepository.save(user);
        return new ResponseModel<UserModel>(model);
    }




    @PostMapping(value="/login")
    @ResponseBody
    public UserModel login(@Validated  @RequestBody UserModel userModel) {
        UserModel user = userRepository.findByUsername(userModel.getUsername());
        if (user.getPassword().equals(userModel.getPassword())) {
            return userModel;
        }

        return null;
    }





}
