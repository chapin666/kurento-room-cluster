package tv.lycam.server.api.response;

/**
 * Created by lycamandroid on 2017/6/20.
 */
public class ResponseModel<T> {

    public boolean success;
    private String errorMsg;
    public T data;


    public ResponseModel(T model) {
        this.data = model;
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
        this.data = model;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
