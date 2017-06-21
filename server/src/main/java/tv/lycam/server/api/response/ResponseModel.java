package tv.lycam.server.api.response;

/**
 * Created by lycamandroid on 2017/6/20.
 */
public class ResponseModel<T> {

    public boolean success;
    private String errorMsg;
    public T model;


    public ResponseModel(T model) {
        this.model = model;
        this.success = true;
    }

    public ResponseModel(String errorMsg) {
        this.success = false;
        this.errorMsg = errorMsg;
    }

    public ResponseModel(boolean status, String errorMsg) {
        this.success = status;
        this.errorMsg = errorMsg;
    }

    public ResponseModel(boolean status, String errorMsg, T model) {
        this.success = status;
        this.errorMsg = errorMsg;
        this.model = model;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }
}
