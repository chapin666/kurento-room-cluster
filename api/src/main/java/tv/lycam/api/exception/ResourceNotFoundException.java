package tv.lycam.api.exception;

/**
 * Created by chengbin on 2017/6/7.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersion = 1L;

    /**
     * 
     * @param msg
     */
    public ResourceNotFoundException(String msg) {
        super(msg);
    }

}

