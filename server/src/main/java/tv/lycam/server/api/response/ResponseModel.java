package tv.lycam.server.api.response;

/**
 * Created by lycamandroid on 2017/6/20.
 */
public class ResponseModel<T> {

    public boolean status;
    private String errorMsg;
    public T model;


    public ResponseModel(T model) {
        this.model = model;
        this.status = true;
    }

    public ResponseModel(String errorMsg) {
        this.status = false;
        this.errorMsg = errorMsg;
    }

    public ResponseModel(boolean status, String errorMsg) {
        this.status = status;
        this.errorMsg = errorMsg;
    }

    public ResponseModel(boolean status, String errorMsg, T model) {
        this.status = status;
        this.errorMsg = errorMsg;
        this.model = model;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
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
