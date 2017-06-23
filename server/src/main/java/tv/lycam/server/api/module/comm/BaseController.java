package tv.lycam.server.api.module.comm;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import tv.lycam.server.api.response.ResponseModel;

/**
 * Created by lycamandroid on 2017/6/23.
 */
public class BaseController {

    protected ResponseEntity sendValidateError(BindingResult bindingResult) {
        String err = bindingResult.getFieldError().getDefaultMessage();
        return ResponseEntity.ok(new ResponseModel<>(err));
    }

}
